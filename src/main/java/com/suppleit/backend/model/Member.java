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
    public void setMemberRole(String role) {
        this.memberRole = MemberRole.fromString(role);  // ✅ NULL 값도 변환 가능하도록 보장
    }
}
