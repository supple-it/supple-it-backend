package com.suppleit.backend.dto;

import com.suppleit.backend.constants.SocialType;
import com.suppleit.backend.model.MemberSocial;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSocialDto {
    private String memberId;
    private String socialId;
    private SocialType socialType;

    // ✅ MemberSocial → MemberSocialDto 변환 메서드
    public static MemberSocialDto fromEntity(MemberSocial memberSocial) {
        return MemberSocialDto.builder()
                .memberId(memberSocial.getMemberId())
                .socialId(memberSocial.getSocialId())
                .socialType(memberSocial.getSocialType())
                .build();
    }
}
