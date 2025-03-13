package com.suppleit.backend.model;

import com.suppleit.backend.constants.Gender;
import com.suppleit.backend.constants.MemberRole;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {
    private String memberId;  // Auto_Increment
    private String email;
    private String password;  // 비밀번호는 반드시 암호화된 값으로 저장
    private String nickname;
    private Gender gender;
    private Date birth;
    private MemberRole memberRole; 

    // ✅ ENUM → String 변환 메서드 (MyBatis에서 ENUM을 올바르게 처리하도록 설정)
    public String getGenderString() {
        return gender != null ? gender.name() : null;
    }

    // ✅ ENUM → String 변환 메서드 추가 (memberRole이 null이면 기본값 "USER" 설정)
    public String getMemberRoleString() {
        return memberRole != null ? memberRole.getRole() : "USER"; // 기본값 설정
    }

    // ✅ ENUM Setter 추가 (MyBatis에서 문자열을 ENUM으로 변환할 때 필요)
    public void setMemberRole(String role) {
        this.memberRole = (role != null) ? MemberRole.from(role) : MemberRole.USER;
    }
}
