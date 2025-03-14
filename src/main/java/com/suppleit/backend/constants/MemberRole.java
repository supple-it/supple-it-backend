package com.suppleit.backend.constants;

public enum MemberRole {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;

    MemberRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MemberRole fromString(String value) {
        if (value == null || value.isEmpty()) {
            return USER;  // ✅ 기본값 USER 설정
        }
        for (MemberRole role : MemberRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {  // ✅ 대소문자 무시하고 변환
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
