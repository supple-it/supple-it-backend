// src/main/java/com/suppleit/backend/controller/MemberController.java
package com.suppleit.backend.controller;

import com.suppleit.backend.dto.MemberDto;
import com.suppleit.backend.service.MemberService;
import com.suppleit.backend.securityFilter.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> joinMember(@RequestBody MemberDto memberDto) {
        memberService.insertMember(memberDto);
        return ResponseEntity.ok("success");
    }
    // 닉네임 중복 검사
    @GetMapping(value = "/join/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable String nickname) {
        boolean isAvailable = memberService.checkNickname(nickname);
        return ResponseEntity.ok(isAvailable);
    }
    // 이메일 중복 검사
    @GetMapping(value = "/join/email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        boolean isAvailable = memberService.checkEmail(email);
        return ResponseEntity.ok(isAvailable);
    }
    // JWT 토큰 기반으로 회원 정보 조회
    @GetMapping("/auth/info")
    public ResponseEntity<MemberDto> getMemberInfo(HttpServletRequest request) {
        String token = parseBearerToken(request);
        if (token == null || jwtTokenProvider.isJwtExpired(token)) {
            return ResponseEntity.status(401).build();
        }
        //토큰 회원정보 (email) 조회
        String email = jwtTokenProvider.getEmail(token);
        MemberDto member = memberService.getMemberByEmail(email);
        return Optional.ofNullable(member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // 회원 조회 (이메일 기반)
    @GetMapping("/auth/{email}")
    public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
        MemberDto member = memberService.getMemberByEmail(email);
        return Optional.ofNullable(member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 아이디(이메일) 찾기 (이메일 입력)
    @GetMapping("/find/email")
    public ResponseEntity<String> findEmail(@RequestParam String email) {
        boolean exists = memberService.checkEmailExists(email);
        if (exists) {
            return ResponseEntity.ok("이미 등록된 회원입니다. 비밀번호 찾기를 진행해주세요.");
        } else {
            return ResponseEntity.ok("입력하신 이메일로 등록된 회원이 없습니다.");
        }
    }


    // 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/find/password")
    public ResponseEntity<String> findPassword(@RequestParam String email) {
        boolean isVerified = memberService.checkEmailExists(email);
        if (isVerified) {
            String tempPassword = memberService.generateTempPassword(email);
            return ResponseEntity.ok("임시 비밀번호가 발급되었습니다: " + tempPassword);
        } else {
            return ResponseEntity.ok("등록되지 않은 이메일입니다.");
        }
    }


    // 회원 탈퇴
    @DeleteMapping("/auth/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    private String parseBearerToken(HttpServletRequest req) {
        String authorization = req.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
