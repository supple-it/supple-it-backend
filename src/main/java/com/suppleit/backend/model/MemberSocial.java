package com.suppleit.backend.model;

import com.suppleit.backend.constants.SocialType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSocial {
    private String memberId;
    private String socialId;
    private SocialType socialType;  // ✅ socialType 필드 추가
}
