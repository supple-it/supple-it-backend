package com.suppleit.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suppleit.backend.security.MemberDetailsService;
import com.suppleit.backend.security.jwt.JwtFilter;
import com.suppleit.backend.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberDetailsService memberDetailsService;

    // ✅ JSON 직렬화를 위한 ObjectMapper
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    // ✅ 비밀번호 암호화 (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())  // CSRF 보호 비활성화 (REST API)
            .cors(cors -> cors.configurationSource(corsSource()))  // CORS 설정 활성화
            .formLogin(form -> form.disable())  // 기본 로그인 폼 비활성화
            .httpBasic(basic -> basic.disable())  // HTTP Basic 인증 비활성화
            .authorizeHttpRequests(this::configureAuthorization)  // 요청별 권한 설정
            .addFilterBefore(new JwtFilter(jwtTokenProvider, memberDetailsService), 
                    UsernamePasswordAuthenticationFilter.class)  // ✅ JWT 필터 적용
            .logout(this::configureLogout);  // 로그아웃 설정

        return http.build();
    }
    
    // ✅ JWT 필터를 Bean으로 등록
    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtTokenProvider, memberDetailsService);
    }

    // ✅ 요청별 권한 설정 (ENUM 활용)
    private void configureAuthorization(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        auth
            .requestMatchers("/admin/**").hasRole("ADMIN")  // 기존: hasRole("ROLE_ADMIN")
            .requestMatchers("/api/member/auth/**").hasAnyRole("ADMIN", "USER")  // 기존: hasAnyRole("ROLE_ADMIN", "ROLE_USER")
            .anyRequest().permitAll();
        
    }

    // ✅ 로그아웃 설정 추가
    private void configureLogout(LogoutConfigurer<HttpSecurity> logout) {
        logout
            .logoutUrl("/api/logout")
            .logoutSuccessHandler((request, response, authentication) -> {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"message\": \"Logout successful\"}");
                response.getWriter().flush();
            });
    }

    // ✅ CORS 설정 보완 (특정 도메인만 허용)
    @Bean
    public CorsConfigurationSource corsSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);  // 인증 정보(쿠키, 세션) 포함 허용
        corsConfig.addAllowedHeader("*");  // 모든 HTTP 헤더 허용
        corsConfig.addAllowedMethod("*");  // 모든 HTTP 메서드 허용
        corsConfig.setAllowedOriginPatterns(List.of("http://localhost:3000"));  // ✅ 특정 도메인만 허용 (보안 강화)
        corsConfig.addExposedHeader("Authorization");  // 클라이언트에서 Authorization 헤더 접근 가능

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
