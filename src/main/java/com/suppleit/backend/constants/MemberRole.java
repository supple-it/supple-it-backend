package com.suppleit.backend.constants;

import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String role;

    MemberRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public static MemberRole from(String role) {
        for (MemberRole memberRole : MemberRole.values()) {
            if (memberRole.role.equalsIgnoreCase(role)) {
                return memberRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }

    @MappedTypes(MemberRole.class)
    public static class TypeHandler extends BaseTypeHandler<MemberRole> {
        @Override
        public void setNonNullParameter(PreparedStatement ps, int i, MemberRole parameter, JdbcType jdbcType) throws SQLException {
            ps.setString(i, parameter.getRole());
        }

        @Override
        public MemberRole getNullableResult(ResultSet rs, String columnName) throws SQLException {
            String role = rs.getString(columnName);
            return role == null ? MemberRole.USER : MemberRole.from(role);  // ✅ 기본값 USER 설정
        }

        @Override
        public MemberRole getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
            String role = rs.getString(columnIndex);
            return role == null ? MemberRole.USER : MemberRole.from(role);  // ✅ 기본값 USER 설정
        }

        @Override
        public MemberRole getNullableResult(java.sql.CallableStatement cs, int columnIndex) throws SQLException {
            String role = cs.getString(columnIndex);
            return role == null ? MemberRole.USER : MemberRole.from(role);  // ✅ 기본값 USER 설정
        }
    }
}
