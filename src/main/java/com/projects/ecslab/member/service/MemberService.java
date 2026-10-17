package com.projects.ecslab.member.service;

import com.projects.ecslab.member.dto.SignupRequest;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(SignupRequest request){
        if(memberRepository.existsByUsername(request.username())){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 아이디입니다."
            );
        }

        Member member = Member.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .build();

        memberRepository.save(member);
    }
}
