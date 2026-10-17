package com.projects.ecslab.member.dto;

import com.projects.ecslab.member.entity.Member;

public record MemberResponse(
        Long id,
        String username
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getUsername()
        );
    }
}