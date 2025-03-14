package com.suppleit.backend.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.suppleit.backend.dto.MemberDto;
import com.suppleit.backend.security.jwt.JwtTokenProvider;
import com.suppleit.backend.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 회원가입
    @PostMapping("/join")
    public ResponseEntity<String> joinMember(@RequestBody MemberDto memberDto) {
        memberService.insertMember(memberDto);
        return ResponseEntity.ok("success");
    }

    // ✅ 닉네임 중복 검사
    @GetMapping(value = "/join/nickname/{nickname}")
    public ResponseEntity<Boolean> checkNickname(@PathVariable String nickname) {
        boolean isAvailable = memberService.checkNickname(nickname);
        return ResponseEntity.ok(isAvailable);
    }

    // ✅ 이메일 중복 검사
    @GetMapping(value = "/join/email/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        boolean isAvailable = memberService.checkEmail(email);
        return ResponseEntity.ok(isAvailable);
    }

    // ✅ JWT 토큰 기반으로 회원 정보 조회
    @GetMapping("/auth/info")
    public ResponseEntity<MemberDto> getMemberInfo(HttpServletRequest request) {
        String token = parseBearerToken(request);
        if (token == null || jwtTokenProvider.isJwtExpired(token)) {
            return ResponseEntity.status(401).build();
        }
        String email = jwtTokenProvider.getEmail(token);
        MemberDto member = memberService.getMemberByEmail(email);
        return Optional.ofNullable(member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ 회원 조회 (이메일 기반)
    @GetMapping("/auth/{email}")
    public ResponseEntity<MemberDto> getMemberByEmail(@PathVariable String email) {
        MemberDto member = memberService.getMemberByEmail(email);
        return Optional.ofNullable(member)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ 비밀번호 찾기 (임시 비밀번호 발급 후 이메일 전송)
    @PostMapping("/find/password")
    public ResponseEntity<String> findPassword(@RequestParam String email) {
        boolean isVerified = memberService.checkEmail(email);
        if (isVerified) {
            memberService.generateTempPassword(email);
            return ResponseEntity.ok("임시 비밀번호가 이메일로 발송되었습니다.");
        } else {
            return ResponseEntity.badRequest().body("등록되지 않은 이메일입니다.");
        }
    }
    /* 
     // 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/find/password")
    public ResponseEntity<String> findPassword(@RequestParam String email) {
        boolean isVerified = memberService.checkEmail(email);
        if (isVerified) {
            String tempPassword = memberService.generateTempPassword(email);
            return ResponseEntity.ok("임시 비밀번호가 발급되었습니다: " + tempPassword);
        } else {
            return ResponseEntity.ok("등록되지 않은 이메일입니다.");
        }
    }
     */
    // ✅ 비밀번호 변경
    @PostMapping("/change-password")
public ResponseEntity<String> changePassword(@RequestBody Map<String, String> request, HttpServletRequest req) {
    String token = parseBearerToken(req);
    if (token == null || jwtTokenProvider.isJwtExpired(token)) {
        return ResponseEntity.status(401).body("인증이 필요합니다.");
    }

    String email = jwtTokenProvider.getEmail(token);
    String oldPassword = request.get("oldPassword");
    String newPassword = request.get("newPassword");

    try {
        boolean isChanged = memberService.changePassword(email, oldPassword, newPassword);
        if (isChanged) {
            return ResponseEntity.ok("비밀번호가 변경되었습니다.");
        }
    } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage()); // 예외 메시지를 클라이언트에 반환
    }

    return ResponseEntity.badRequest().body("비밀번호 변경에 실패하였습니다.");
}


    /* // ✅ 회원 탈퇴 (AUTO_INCREMENT 적용)
    @DeleteMapping("/auth/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Integer memberId) {  // ✅ String → Integer 변경
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    } */
    // ✅ 회원 탈퇴 (로그인한 사용자만 자신의 계정 삭제 가능)
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMember(HttpServletRequest req) {
        // 1️⃣ JWT 토큰을 통해 현재 로그인한 사용자의 이메일 가져오기
        String token = parseBearerToken(req);
        if (token == null || jwtTokenProvider.isJwtExpired(token)) {
            return ResponseEntity.status(401).body("인증이 필요합니다.");
        }

        // 2️⃣ 토큰에서 이메일 추출
        String email = jwtTokenProvider.getEmail(token);

        // 3️⃣ 회원 탈퇴 수행
        boolean isDeleted = memberService.deleteMemberByEmail(email);

        if (isDeleted) {
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } else {
            return ResponseEntity.status(400).body("회원 탈퇴에 실패했습니다.");
        }
    }
    // ✅ 로그아웃 (토큰 무효화)
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest req) {
        String token = parseBearerToken(req);
        if (token == null || jwtTokenProvider.isJwtExpired(token)) {
            return ResponseEntity.status(401).body("이미 로그아웃되었거나 유효하지 않은 토큰입니다.");
        }
        
        // 클라이언트에서 토큰 삭제하도록 응답
        return ResponseEntity.ok("로그아웃이 완료되었습니다.");
    }
    private String parseBearerToken(HttpServletRequest req) {
        String authorization = req.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
