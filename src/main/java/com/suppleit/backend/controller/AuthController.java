package com.suppleit.backend.controller;

import com.suppleit.backend.dto.AuthRequest;
import com.suppleit.backend.security.jwt.JwtTokenProvider;
import com.suppleit.backend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 로그인 API (JWT 발급)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        String token = memberService.authenticate(authRequest.getEmail(), authRequest.getPassword());
        return ResponseEntity.ok().body("{\"accessToken\": \"" + token + "\"}");
    }
}
