package com.suppleit.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            String token = resolveToken(request);

            if (token != null) {
                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Invalid or expired token");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }

                String email = jwtTokenProvider.getEmail(token);
                String role = jwtTokenProvider.getRole(token);

                // 로깅 추가
                log.info("JWT Token - Email: {}, Role: {}", email, role);

                // 역할이 없는 경우 기본값 설정
                if (!StringUtils.hasText(role)) {
                    log.warn("No role found in token, defaulting to ROLE_USER");
                    role = "ROLE_USER";
                }

                // ROLE_ prefix 확인 및 추가
                if (!role.startsWith("ROLE_")) {
                    role = "ROLE_" + role;
                }

                List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(role)
                );

                log.info("Granted Authority: {}", role);

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails != null) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        authorities
                    );
                    
                    ((UsernamePasswordAuthenticationToken) auth).setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("No user details found for email: {}", email);
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Filter Error", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error: " + e.getMessage());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null) {
            return null;
        }
        if (!bearerToken.startsWith("Bearer ")) {
            log.warn("Invalid token format: {}", bearerToken);
            throw new IllegalArgumentException("유효하지 않은 토큰 형식입니다.");
        }
        return bearerToken.substring(7);
    }
}