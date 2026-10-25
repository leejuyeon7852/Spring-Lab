package com.projects.ecslab.global.config;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.global.jwt.JwtAuthenticationFilter;
import com.projects.ecslab.global.jwt.JwtTokenProvider;
import com.projects.ecslab.global.oauth2.OAuth2LoginSuccessHandler;
import com.projects.ecslab.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 담당자
    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    // 인증 요청
    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(authenticationProvider);
    }

    // 접근 제어
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            MemberRepository memberRepository,
            @Qualifier("handlerExceptionResolver")
            HandlerExceptionResolver exceptionResolver,
            OAuth2LoginSuccessHandler oauth2LoginSuccessHandler,
            OidcUserService oidcUserService
    ) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                HttpMethod.POST, "/api/members/signup", "/api/members/login",
                                "/api/members/refresh"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .requestMatchers("/api/public").permitAll()
                        .requestMatchers("/api/member").authenticated()
                        .requestMatchers("/api/admin").hasRole("ADMIN")
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
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
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oauth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) ->
                                exceptionResolver.resolveException(
                                        request,
                                        response,
                                        null,
                                        new BusinessException(ResponseCode.SOCIAL_LOGIN_FAILED)
                                )
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService)
                        )
                )
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

    @Bean
    public OidcUserService oidcUserService() {
        DefaultOAuth2UserService profileService = new DefaultOAuth2UserService();

        profileService.setAttributesConverter(userRequest -> attributes -> {
            String registrationId = userRequest.getClientRegistration()
                    .getRegistrationId();

            if (!"naver".equals(registrationId)) {
                return attributes;
            }

            Object response = attributes.get("response");

            if (!(response instanceof Map<?, ?> profile)
                    || !(profile.get("id") instanceof String id)
                    || id.isBlank()) {
                throw new IllegalArgumentException(
                        "네이버 프로필 식별자가 없습니다."
                );
            }

            Map<String, Object> converted = new HashMap<>();
            converted.put("sub", id);

            if (profile.get("email") instanceof String email) {
                converted.put("email", email);
            }

            return converted;
        });

        OidcUserService service = new OidcUserService();
        service.setOauth2UserService(profileService);
        return service;
    }
}
