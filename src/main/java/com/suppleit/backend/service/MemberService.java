package com.suppleit.backend.service;

import com.suppleit.backend.constants.MemberRole;
import com.suppleit.backend.dto.MemberDto;
import com.suppleit.backend.mapper.MemberMapper;
import com.suppleit.backend.model.Member;
import com.suppleit.backend.security.jwt.JwtTokenProvider; 
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // ✅ 회원가입 (MyBatis 적용)
    public void insertMember(MemberDto memberDto) {
        // ✅ 이메일 중복 검사
        if (checkEmailExists(memberDto.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

         // ✅ 기본 역할이 설정되지 않았다면 "USER"로 설정
        MemberRole role = memberDto.getMemberRole() != null ? memberDto.getMemberRole() : MemberRole.USER;
    
        Member member = Member.builder()
                .email(memberDto.getEmail())
                .password(passwordEncoder.encode(memberDto.getPassword()))  // ✅ 비밀번호 암호화 추가
                .nickname(memberDto.getNickname())
                .gender(memberDto.getGender())
                .birth(memberDto.getBirth())
                .memberRole(role)
                .build();
    
        memberMapper.insertMember(member);  // ✅ Member 객체를 전달
        
        System.out.println("새로운 회원 ID: " + member.getMemberId()); // ✅ 생성된 memberId 확인
        System.out.println("저장된 암호화 비밀번호: " + member.getPassword());  // ✅ 저장된 비밀번호 확인
    }
    
    // ✅ 이메일 중복 검사
    public boolean checkEmail(String email) {
        return memberMapper.checkEmail(email);
    }

    // ✅ 닉네임 중복 검사
    public boolean checkNickname(String nickname) {
        return memberMapper.checkNickname(nickname);
    }

    // ✅ 이메일로 회원 조회
    public MemberDto getMemberByEmail(String email) {
        Member member = memberMapper.getMemberByEmail(email);
        return (member != null) ? MemberDto.fromEntity(member) : null;
    }    

    // ✅ 이메일 존재 여부 확인
    public boolean checkEmailExists(String email) {
        return memberMapper.checkEmail(email);
    }

    // ✅ 회원 삭제 (memberId 기반으로 삭제)
    public void deleteMember(Integer memberId) {
        if (memberMapper.getMemberById(memberId) == null) {  // ✅ memberId 기반 조회
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        memberMapper.deleteMember(memberId);
    }

    // ✅ 임시 비밀번호 발급
    public String generateTempPassword(String email) {
        Member member = memberMapper.getMemberByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }

        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        memberMapper.updatePassword(email, passwordEncoder.encode(tempPassword));

        return tempPassword;
    }
    // ✅ 인증 (로그인 및 JWT 생성)
    public String authenticate(String email, String password) {
        Member member = memberMapper.getMemberByEmail(email);
        
        if (member == null) {
            throw new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다.");  // ✅ 이메일이 존재하는지 확인
        }
    
        System.out.println("DB 저장된 비밀번호: " + member.getPassword());  // ✅ 저장된 비밀번호 출력
        System.out.println("입력된 비밀번호: " + password);  // ✅ 사용자가 입력한 비밀번호 출력
        System.out.println("비밀번호 일치 여부: " + passwordEncoder.matches(password, member.getPassword()));  // ✅ 비교 결과 출력
    
        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");  // ✅ 비밀번호 불일치 메시지 분리
        }
         // ✅ memberRole이 null이면 기본값 "USER" 설정
        MemberRole role = (member.getMemberRole() != null) ? member.getMemberRole() : MemberRole.USER;
    
        return jwtTokenProvider.createToken(email, role.name());
    }

    
}
