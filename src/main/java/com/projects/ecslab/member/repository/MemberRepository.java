package com.projects.ecslab.member.repository;

import com.projects.ecslab.member.dto.AuthProvider;
import com.projects.ecslab.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    boolean existsByUsername(String username);

    Optional<Member> findByUsername(String username);

    Optional<Member> findByProviderAndProviderId(
            AuthProvider provider,
            String providerId
    );
}
