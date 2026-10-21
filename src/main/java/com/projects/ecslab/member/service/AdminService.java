package com.projects.ecslab.member.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @PreAuthorize("hasRole('ADMIN')")
    public String getAdminMessage() {
        return "관리자 전용 기능입니다.";
    }
}