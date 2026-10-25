package com.projects.ecslab.global.oauth2;

import com.projects.ecslab.global.dto.BaseResponse;
import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.member.dto.LoginResponse;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.service.SocialMemberService;
import com.projects.ecslab.member.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final SocialMemberService socialMemberService;
    private final TokenService tokenService;
    private final JsonMapper jsonMapper;
    private final SocialUserInfoExtractor socialUserInfoExtractor;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthToken.getPrincipal();

        SocialUserInfo userInfo = socialUserInfoExtractor.extract(
                registrationId,
                oauthUser
        );

        Member member = socialMemberService.findOrCreate(
                userInfo.provider(),
                userInfo.providerId(),
                userInfo.email()
        );

        LoginResponse tokens = tokenService.issue(member);

        response.setStatus(ResponseCode.LOGIN_SUCCESS.httpStatus().value());
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        jsonMapper.writeValue(
                response.getWriter(),
                BaseResponse.success(ResponseCode.LOGIN_SUCCESS, tokens)
        );
    }
}