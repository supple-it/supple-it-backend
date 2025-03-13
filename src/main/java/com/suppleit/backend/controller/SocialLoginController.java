package com.suppleit.backend.controller;

import com.suppleit.backend.dto.MemberSocialDto;
import com.suppleit.backend.service.SocialLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialLoginController {

    private final SocialLoginService socialLoginService;

    // ✅ 카카오 로그인
    @PostMapping("/login/kakao")
    public ResponseEntity<MemberSocialDto> loginWithKakao(@RequestParam String accessToken) {
        MemberSocialDto memberSocial = socialLoginService.getKakaoMember(accessToken);
        return ResponseEntity.ok(memberSocial);
    }

    // ✅ 구글 로그인
    @PostMapping("/login/google")
    public ResponseEntity<MemberSocialDto> loginWithGoogle(@RequestParam String accessToken) {
        MemberSocialDto memberSocial = socialLoginService.getGoogleMember(accessToken);
        return ResponseEntity.ok(memberSocial);
    }

    // ✅ 네이버 로그인
    @PostMapping("/login/naver")
    public ResponseEntity<MemberSocialDto> loginWithNaver(@RequestParam String accessToken) {
        MemberSocialDto memberSocial = socialLoginService.getNaverMember(accessToken);
        return ResponseEntity.ok(memberSocial);
    }
}
