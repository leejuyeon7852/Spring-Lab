package com.projects.ecslab.member.service;

import com.projects.ecslab.global.jwt.JwtTokenProvider;
import com.projects.ecslab.member.dto.LoginResponse;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.entity.RefreshToken;
import com.projects.ecslab.member.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse issue(Member member) {
        // 새 accessToken, refresh Token 발급
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        // 토큰 hash
        String tokenHash = hashToken(refreshToken);
        // 만료일
        Instant expiresAt = jwtTokenProvider.getExpiration(refreshToken);

        RefreshToken savedToken = refreshTokenRepository
                .findByMember_Id(member.getId())
                .orElse(null);

        if (savedToken == null) {
            refreshTokenRepository.save(
                    new RefreshToken(member, tokenHash, expiresAt)
            );
        } else {
            // 교체
            savedToken.rotate(tokenHash, expiresAt);
        }

        return new LoginResponse(accessToken, refreshToken);
    }

    // refresh token 해시 생성
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 알고리즘을 사용할 수 없습니다.",
                    exception
            );
        }
    }
}