package com.projects.ecslab.member.entity;

import com.projects.ecslab.member.dto.AuthProvider;
import com.projects.ecslab.member.dto.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_provider_provider_id",
                        columnNames = {"provider", "provider_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    @Column
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    private Member(
            String username,
            String password,
            String email,
            AuthProvider provider,
            String providerId
    ) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.role = Role.USER;
    }

    public void fillEmailIfAbsent(String email){
        if((this.email == null || this.email.isBlank()) && email != null && !email.isBlank()){
            this.email = email;
        }
    }
}
