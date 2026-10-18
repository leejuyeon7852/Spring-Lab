package com.projects.ecslab.member.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
