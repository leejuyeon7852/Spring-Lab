package com.projects.ecslab.member.service;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.member.dto.MemberResponse;
import com.projects.ecslab.member.dto.SignupRequest;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MemberResponse signup(SignupRequest request){
        if (memberRepository.existsByUsername(request.username())) {
            throw new BusinessException(ResponseCode.DUPLICATE_USERNAME);
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        memberRepository.save(member);

        MemberResponse response = MemberResponse.from(member);

        return response;
    }
}
