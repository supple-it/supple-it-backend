package com.suppleit.backend.constants;

public enum Gender {
    MALE("male"),  // ✅ DB 값과 일치하도록 수정
    FEMALE("female");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Gender fromString(String value) {
        for (Gender gender : Gender.values()) {
            if (gender.value.equalsIgnoreCase(value)) {  // ✅ 대소문자 구분 없이 변환
                return gender;
            }
        }
        throw new IllegalArgumentException("Invalid gender value: " + value);
    }
}
