package com.projects.ecslab.global.oauth2;

import com.projects.ecslab.member.dto.AuthProvider;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SocialUserInfoExtractor {

    public SocialUserInfo extract(
            String registrationId,
            OAuth2User oauthUser
    ) {
        if ("google".equals(registrationId)) {
            OidcUser oidcUser = (OidcUser) oauthUser;

            return new SocialUserInfo(
                    AuthProvider.GOOGLE,
                    oidcUser.getSubject(),
                    oidcUser.getEmail()
            );
        }

        if ("naver".equals(registrationId)) {
            OidcUser oidcUser = (OidcUser) oauthUser;

            return new SocialUserInfo(
                    AuthProvider.NAVER,
                    oidcUser.getSubject(),
                    oidcUser.getEmail()
            );
        }

        if ("kakao".equals(registrationId)) {
            OidcUser oidcUser = (OidcUser) oauthUser;

            return new SocialUserInfo(
                    AuthProvider.KAKAO,
                    oidcUser.getSubject(),
                    oidcUser.getEmail()
            );
        }

        throw new IllegalStateException(
                "지원하지 않는 로그인 제공자입니다: " + registrationId
        );
    }
}