package com.projects.ecslab.global.oauth2;

import com.projects.ecslab.member.dto.AuthProvider;

public record SocialUserInfo(
        AuthProvider provider,
        String providerId,
        String email
) {
}