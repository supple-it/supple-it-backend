package com.suppleit.backend.service;

import com.suppleit.backend.dto.MemberSocialDto;
import com.suppleit.backend.mapper.MemberSocialMapper;
import com.suppleit.backend.model.MemberSocial;
import com.suppleit.backend.constants.SocialType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoginService {

    private final MemberSocialMapper memberSocialMapper;

    // ✅ 카카오 로그인 처리
    public MemberSocialDto getKakaoMember(String accessToken) {
        String kakaoUserInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<?> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(kakaoUserInfoUrl, HttpMethod.GET, entity, Map.class);

        Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String socialId = String.valueOf(response.getBody().get("id"));

        return processSocialLogin(socialId, email, SocialType.KAKAO);
    }

    // ✅ 구글 로그인 처리
    public MemberSocialDto getGoogleMember(String accessToken) {
        String googleUserInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(googleUserInfoUrl, HttpMethod.GET, entity, Map.class);
    
        Map<String, Object> googleUser = response.getBody();
        String email = (String) googleUser.get("email");
        String socialId = (String) googleUser.get("sub");
    
        return processSocialLogin(socialId, email, SocialType.GOOGLE);
    }

    // ✅ 네이버 로그인 처리
    public MemberSocialDto getNaverMember(String accessToken) {
        String naverUserInfoUrl = "https://openapi.naver.com/v1/nid/me";
    
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
    
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.exchange(naverUserInfoUrl, HttpMethod.GET, entity, Map.class);
    
        Map<String, Object> responseData = (Map<String, Object>) response.getBody().get("response");
        String email = (String) responseData.get("email");
        String socialId = (String) responseData.get("id");
    
        return processSocialLogin(socialId, email, SocialType.NAVER);
    }
    
    

    // ✅ 공통 소셜 로그인 처리 로직
    private MemberSocialDto processSocialLogin(String socialId, String email, SocialType socialType) {
        MemberSocial existingMember = memberSocialMapper.getMemberSocialBySocialId(socialId);

        if (existingMember != null) {
            return MemberSocialDto.fromEntity(existingMember);
        }

        // 신규 회원 등록
        MemberSocial newMember = MemberSocial.builder()
                .memberId(email)  // 이메일을 회원 ID로 사용
                .socialId(socialId)
                .socialType(socialType)
                .build();

        memberSocialMapper.insertMemberSocial(newMember);
        return MemberSocialDto.fromEntity(newMember);
    }
}
