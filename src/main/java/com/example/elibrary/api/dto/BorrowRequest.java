package com.example.elibrary.api.dto;

import com.example.elibrary.domain.MemberId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** 借阅请求。格式规则直接引用领域值对象的常量，避免两处规则漂移。 */
public record BorrowRequest(
        @NotNull @Positive Long bookId,
        @NotBlank
        @Pattern(regexp = MemberId.PATTERN,
                message = "memberId 需字母开头、仅含字母数字下划线、长度 4-32")
        String memberId) {
}
