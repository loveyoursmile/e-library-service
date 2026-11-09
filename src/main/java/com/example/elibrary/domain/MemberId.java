package com.example.elibrary.domain;

import java.util.regex.Pattern;

/**
 * 读者标识值对象。格式规则只有这一处定义，Web 层注解直接引用 PATTERN。
 */
public record MemberId(String value) {

    public static final String PATTERN = "^[A-Za-z][A-Za-z0-9_]{3,31}$";

    private static final Pattern COMPILED = Pattern.compile(PATTERN);

    public MemberId {
        if (value == null || !COMPILED.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "memberId 需字母开头、仅含字母数字下划线、长度 4-32");
        }
    }

    public static MemberId of(String value) {
        return new MemberId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
