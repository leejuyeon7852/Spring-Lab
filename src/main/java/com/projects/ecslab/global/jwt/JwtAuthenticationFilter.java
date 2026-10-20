package com.projects.ecslab.global.jwt;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.repository.MemberRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final HandlerExceptionResolver exceptionResolver;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            MemberRepository memberRepository,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberRepository = memberRepository;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Authorization 헤더 읽기
        String authorization = request.getHeader("Authorization");

        // 토큰 없으면 다음 필터 전달
        if (authorization == null || !authorization.startsWith("Bearer ")){
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 추출
        String token = authorization.substring(7);

        try {
            // 검증 및 회원 조회
            Long memberId = jwtTokenProvider.getMemberId(token, "access");

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.TOKEN_MEMBER_NOT_FOUND));

            // 인증정보 생성
            Authentication authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            member.getId(), null,
                            List.of(
                                    new SimpleGrantedAuthority("ROLE_" + member.getRole().name())
                            )
                    );

            // 인증 정보 등록
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

        }  catch (BusinessException exception) {
            SecurityContextHolder.clearContext();

            exceptionResolver.resolveException(
                    request, response, null, exception
            );
            return;
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();

            exceptionResolver.resolveException(
                    request, response, null, exception
            );
            return;
        } catch (IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();

            exceptionResolver.resolveException(
                    request,
                    response,
                    null,
                    new BusinessException(ResponseCode.INVALID_TOKEN)
            );
            return;
        }

        // 다음 필터 실행
        filterChain.doFilter(request, response);

    }
}
