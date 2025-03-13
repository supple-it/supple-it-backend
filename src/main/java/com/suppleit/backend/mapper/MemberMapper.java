package com.suppleit.backend.mapper;

import com.suppleit.backend.model.Member;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MemberMapper {

    // ✅ 회원 삽입 (AUTO_INCREMENT 사용)
    @Insert("INSERT INTO Member (email, password, nickname, gender, birth, member_role) " +
            "VALUES (#{email}, #{password}, #{nickname}, #{gender}, #{birth}, #{memberRole})")
    @Options(useGeneratedKeys = true, keyProperty = "memberId")  // ✅ AUTO_INCREMENT 적용
    void insertMember(Member member);

    // ✅ 이메일로 회원 조회
    @Select("SELECT * FROM Member WHERE email = #{email}")
    Member getMemberByEmail(@Param("email") String email);

    // ✅ memberId로 회원 조회 추가
    @Select("SELECT * FROM Member WHERE member_id = #{memberId}")
    Member getMemberById(@Param("memberId") Integer memberId);

    // ✅ 이메일 중복 검사
    @Select("SELECT COUNT(*) > 0 FROM Member WHERE email = #{email}")
    boolean checkEmail(@Param("email") String email);

    // ✅ 닉네임 중복 검사
    @Select("SELECT COUNT(*) > 0 FROM Member WHERE nickname = #{nickname}")
    boolean checkNickname(@Param("nickname") String nickname);

    // ✅ 비밀번호 업데이트
    @Update("UPDATE Member SET password = #{password} WHERE email = #{email}")
    void updatePassword(@Param("email") String email, @Param("password") String password);

    // ✅ 회원 삭제 (Integer 타입으로 변경)
    @Delete("DELETE FROM Member WHERE member_id = #{memberId}")
    void deleteMember(@Param("memberId") Integer memberId);
}
