package com.projects.ecslab.global.config;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.global.jwt.JwtAuthenticationFilter;
import com.projects.ecslab.global.jwt.JwtTokenProvider;
import com.projects.ecslab.member.repository.MemberRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 접근 제어
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            MemberRepository memberRepository,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver
    ) throws Exception{
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/members/signup", "/api/members/login")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST, "/api/members/**"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/public").permitAll()
                        .requestMatchers("/api/member").authenticated()
                        .requestMatchers("/api/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(
                        new JwtAuthenticationFilter(
                                jwtTokenProvider,
                                memberRepository,
                                exceptionResolver
                        ),
                        UsernamePasswordAuthenticationFilter.class
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                exceptionResolver.resolveException(
                                        request,response,null,
                                        new BusinessException(
                                                ResponseCode.AUTHENTICATION_REQUIRED
                                        )
                                )

                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                exceptionResolver.resolveException(
                                        request,response,null,
                                        new BusinessException(ResponseCode.ACCESS_DENIED)
                                )
                        )
                );

        return http.build();
    }

}
