package com.projects.ecslab.member.controller;

import com.projects.ecslab.global.dto.BaseResponse;
import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.member.dto.LoginRequest;
import com.projects.ecslab.member.dto.MemberResponse;
import com.projects.ecslab.member.dto.SignupRequest;
import com.projects.ecslab.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<MemberResponse>> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = memberService.signup(request);

        return ResponseEntity
                .status(ResponseCode.MEMBER_CREATED.httpStatus())
                .body(BaseResponse.success(ResponseCode.MEMBER_CREATED, response));
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<MemberResponse>> login(@Valid @RequestBody LoginRequest request){
        MemberResponse response = memberService.login(request);

        return ResponseEntity
                .status(ResponseCode.LOGIN_SUCCESS.httpStatus())
                .body(BaseResponse.success(ResponseCode.LOGIN_SUCCESS, response));
    }
}
