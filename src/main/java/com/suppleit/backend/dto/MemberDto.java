package com.suppleit.backend.dto;

import com.suppleit.backend.model.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.suppleit.backend.constants.Gender;
import com.suppleit.backend.constants.MemberRole;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String email;
    private String password;
    private String nickname;
    private Gender gender;
    private Date birth;
    @JsonProperty("memberRole")  // ✅ JSON 필드명을 명확하게 지정
    private MemberRole memberRole;

    // ✅ Member → MemberDto 변환 메서드 (비밀번호 제외)
    public static MemberDto fromEntity(Member member) {
        return MemberDto.builder()
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .gender(member.getGender())
                .birth(member.getBirth())
                .memberRole(member.getMemberRole())
                .build();
    }

    // ✅ MemberDto → Member 변환 메서드 (회원가입용)
    public Member toEntity(String encodedPassword) {
        return Member.builder()
                .email(this.email)
                .password(encodedPassword)  // 비밀번호는 외부에서 암호화 후 전달해야 함
                .nickname(this.nickname)
                .gender(this.gender)
                .birth(this.birth)
                .memberRole(this.memberRole)
                .build();
    }
}
