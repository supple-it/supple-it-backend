package com.suppleit.backend.mapper;

import com.suppleit.backend.model.MemberSocial;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberSocialMapper {
    void insertMemberSocial(MemberSocial memberSocial);
    MemberSocial getMemberSocialBySocialId(@Param("socialId") String socialId);
}
