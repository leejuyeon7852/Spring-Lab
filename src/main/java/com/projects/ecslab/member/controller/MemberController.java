package com.projects.ecslab.member.controller;

import com.projects.ecslab.global.dto.BaseResponse;
import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.member.dto.*;
import com.projects.ecslab.member.entity.RefreshToken;
import com.projects.ecslab.member.repository.MemberRepository;
import com.projects.ecslab.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);

        return ResponseEntity
                .status(ResponseCode.MEMBER_CREATED.httpStatus())
                .body(BaseResponse.success(ResponseCode.MEMBER_CREATED, response));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        LoginResponse response = memberService.login(request);

        return ResponseEntity
                .status(ResponseCode.LOGIN_SUCCESS.httpStatus())
                .body(BaseResponse.success(ResponseCode.LOGIN_SUCCESS, response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<BaseResponse<LoginResponse>> refreshToken(@Valid @RequestBody RefreshRequest request){
        LoginResponse response = memberService.refresh(request);

        return ResponseEntity
                .status(ResponseCode.TOKEN_REFRESHED.httpStatus())
                .body(BaseResponse.success(ResponseCode.TOKEN_REFRESHED, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(
            @AuthenticationPrincipal Long memberId
    ) {
        memberService.logout(memberId);

        return ResponseEntity
                .status(ResponseCode.LOGOUT_SUCCESS.httpStatus())
                .body(BaseResponse.<Void>success(
                        ResponseCode.LOGOUT_SUCCESS, null
                ));
    }
}
