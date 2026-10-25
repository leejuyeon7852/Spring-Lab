package com.projects.ecslab.member.service;

import com.projects.ecslab.member.dto.AuthProvider;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SocialMemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member findOrCreate(
            AuthProvider provider,
            String providerId,
            String email
    ) {
        Member member =  memberRepository
                .findByProviderAndProviderId(provider, providerId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder()
                                .provider(provider)
                                .providerId(providerId)
                                .email(email)
                                .build()
                ));
        member.fillEmailIfAbsent(email);

        return member;
    }
}