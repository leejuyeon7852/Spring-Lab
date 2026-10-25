package com.projects.ecslab.global.oauth2;

import com.projects.ecslab.member.dto.AuthProvider;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class SocialUserInfoExtractor {

    public SocialUserInfo extract(
            String registrationId,
            OAuth2User oauthUser
    ) {
        AuthProvider provider = switch (registrationId) {
            case "google" -> AuthProvider.GOOGLE;
            case "naver" -> AuthProvider.NAVER;
            case "kakao" -> AuthProvider.KAKAO;
            default -> throw new IllegalStateException(
                    "지원하지 않는 로그인 제공자입니다: " + registrationId
            );
        };

        if (!(oauthUser instanceof OidcUser oidcUser)) {
            throw new IllegalStateException(
                    "OIDC 사용자 정보가 아닙니다."
            );
        }

        return new SocialUserInfo(
                provider,
                oidcUser.getSubject(),
                oidcUser.getEmail()
        );
    }
}