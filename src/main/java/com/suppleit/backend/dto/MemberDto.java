package com.suppleit.backend.dto;

import com.suppleit.backend.entity.Member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberDto {
    private String email;
    private String password;
    private String nickname;

    public static MemberDto fromEntity(Member member) {
        return MemberDto.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .build();
    }
}
