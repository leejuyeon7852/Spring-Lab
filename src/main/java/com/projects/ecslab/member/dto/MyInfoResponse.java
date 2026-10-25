package com.projects.ecslab.member.dto;

import com.projects.ecslab.member.entity.Member;

public record MyInfoResponse(
        Long id,
        String username,
        String email,
        AuthProvider provider
) {
    public static MyInfoResponse from(Member member) {
        return new MyInfoResponse(
                member.getId(),
                member.getUsername(),
                member.getEmail(),
                member.getProvider()
        );
    }
}
