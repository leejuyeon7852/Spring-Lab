package com.projects.ecslab.global.config;

import com.projects.ecslab.global.dto.ResponseCode;
import com.projects.ecslab.global.exception.BusinessException;
import com.projects.ecslab.global.jwt.JwtAuthenticationFilter;
import com.projects.ecslab.global.jwt.JwtTokenProvider;
import com.projects.ecslab.member.dto.AuthProvider;
import com.projects.ecslab.member.dto.LoginResponse;
import com.projects.ecslab.member.entity.Member;
import com.projects.ecslab.member.repository.MemberRepository;
import com.projects.ecslab.member.service.SocialMemberService;
import com.projects.ecslab.member.service.TokenService;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

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
            SocialMemberService socialMemberService,
            TokenService tokenService
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
                        .successHandler((request, response, authentication) -> {
                            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

                            Member member = socialMemberService.findOrCreate(
                                    AuthProvider.GOOGLE,
                                    oidcUser.getSubject(),
                                    oidcUser.getEmail()
                            );

                            LoginResponse tokens = tokenService.issue(member);

                            response.setContentType("application/json;charset=UTF-8");
                            response.setHeader("Cache-Control", "no-store");
                            response.getWriter().write(
                                    """
                                    {
                                      "accessToken": "%s",
                                      "refreshToken": "%s"
                                    }
                                    """.formatted(
                                            tokens.accessToken(),
                                            tokens.refreshToken()
                                    )
                            );
                        }))
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
