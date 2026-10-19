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

    @Transactional
    public MemberResponse signup(SignupRequest request){
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        memberRepository.save(member);

        MemberResponse response = MemberResponse.from(member);

        return response;
    }

    @Transactional
    public LoginResponse login(LoginRequest request){
        Member member = memberRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ResponseCode.INVALID_CREDENTIALS));

        if(!passwordEncoder.matches(request.password(), member.getPassword())){
            throw new BusinessException(ResponseCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        String tokenHash = hashToken(refreshToken);
        Instant expireAt = jwtTokenProvider.getExpiration(refreshToken);

        RefreshToken savedToken = refreshTokenRepository
                .findByMember_Id(member.getId())
                .orElse(null);

        if(savedToken == null){
            refreshTokenRepository.save(
                    new RefreshToken(member, tokenHash, expireAt)
            );
        } else {
            savedToken.rotate(tokenHash, expireAt);
        }

        return new LoginResponse(accessToken, refreshToken);
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
        String tokenHash = hashToken(request.refreshToken());
        if(!savedToken.getTokenHash().equals(tokenHash)){
            throw new BusinessException(ResponseCode.INVALID_TOKEN);
        }

        // 새 accessToken, refresh Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(memberId);
        String refreshToken = jwtTokenProvider.createRefreshToken(memberId);

        // 교체
        savedToken.rotate(
                hashToken(refreshToken),
                jwtTokenProvider.getExpiration(refreshToken)
        );

        return new LoginResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long memberId){
        refreshTokenRepository.deleteByMember_Id(memberId);
    }

    // refresh token 해시 생성
    private String hashToken(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        }catch (NoSuchAlgorithmException exception){
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.", exception
            );
        }
    }
}
