package com.example.elibrary.api.dto;

import com.example.elibrary.application.LoanView;
import java.time.LocalDateTime;

/** 借阅 / 归还 / 在借列表的响应。 */
public record LoanResponse(
        long loanId,
        long bookId,
        String bookTitle,
        String bookType,
        String memberId,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        String status,
        LocalDateTime returnedAt,
        boolean overdue) {

    public static LoanResponse from(LoanView view) {
        return new LoanResponse(view.loanId().value(), view.bookId().value(), view.bookTitle(),
                view.bookType(), view.memberId().value(), view.borrowedAt(), view.dueAt(),
                view.status().name(), view.returnedAt(), view.overdue());
    }
}
