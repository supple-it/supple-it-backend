package com.suppleit.backend.service;

import com.suppleit.backend.dto.MemberDto;
import com.suppleit.backend.entity.Member;
import com.suppleit.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public void insertMember(MemberDto memberDto) {
        Member member = Member.builder()
                .email(memberDto.getEmail())
                .password(passwordEncoder.encode(memberDto.getPassword()))
                .nickname(memberDto.getNickname())
                .build();
        memberRepository.save(member);
    }

    // 이메일 중복 검사
    public boolean checkEmail(String email) {
        return !memberRepository.existsByEmail(email);
    }

    // 닉네임 중복 검사
    public boolean checkNickname(String nickname) {
        return !memberRepository.existsByNickname(nickname);
    }

    // 이메일로 회원 조회
    public MemberDto getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .map(MemberDto::fromEntity)
                .orElse(null);
    }

    // 이메일 존재 여부 확인
    public boolean checkEmailExists(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 회원 삭제
    public void deleteMember(Long memberId) {
        memberRepository.deleteById(memberId);
    }

    // 임시 비밀번호 발급
    public String generateTempPassword(String email) {
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        memberRepository.findByEmail(email).ifPresent(member -> {
            member.setPassword(passwordEncoder.encode(tempPassword));
            memberRepository.save(member);
        });
        return tempPassword;
    }
}
