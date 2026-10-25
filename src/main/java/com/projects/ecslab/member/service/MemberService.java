package com.projects.ecslab.member.service;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.global.jwt.JwtTokenProvider;
import com.projects.ecslab.member.dto.*;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.entity.RefreshToken;
import com.projects.ecslab.member.repository.MemberRepository;
import com.projects.ecslab.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Transactional
    public MemberResponse signup(SignupRequest request){
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .build();

        memberRepository.save(member);

        MemberResponse response = MemberResponse.from(member);

        return response;
    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        Authentication authentication;
        try{
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(),
                            request.password()
                    )
            );
        }catch(BadCredentialsException exception){
            throw new BusinessException(ResponseCode.INVALID_CREDENTIALS);
        }

        Member member = memberRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new BusinessException(ResponseCode.INVALID_CREDENTIALS));

        return tokenService.issue(member);
    }

    @Transactional
    public LoginResponse refresh(RefreshRequest request){
        // Refresh Token 검증
        Long memberId = jwtTokenProvider.getMemberId(
                request.refreshToken(), "refresh"
        );

        // memberId로 저장된 RefreshToken 조회
        RefreshToken savedToken = refreshTokenRepository
                .findByMember_Id(memberId)
                .orElseThrow(() -> new BusinessException(ResponseCode.INVALID_TOKEN));

        // 받은 토큰 hash
        String tokenHash = tokenService.hashToken(request.refreshToken());
        if(!savedToken.getTokenHash().equals(tokenHash)){
            throw new BusinessException(ResponseCode.INVALID_TOKEN);
        }

        // 새 accessToken, refresh Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        // 교체
        savedToken.rotate(
                tokenService.hashToken(refreshToken),
                jwtTokenProvider.getExpiration(refreshToken)
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long memberId){
        refreshTokenRepository.deleteByMember_Id(memberId);
    }

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long memberId){
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ResponseCode.TOKEN_MEMBER_NOT_FOUND));

        return MyInfoResponse.from(member);
    }

}
