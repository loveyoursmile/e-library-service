package com.example.elibrary.domain;

import java.time.LocalDateTime;

/**
 * 借阅记录聚合根：一次借书事实。跨聚合只用 ID 引用 Book 与读者。
 *
 * <p>状态只能 ACTIVE → RETURNED；到期日在签发时算好并随记录保存，
 * 以后修改借期策略不会改写历史记录。
 */
public record Loan(
        LoanId id,
        BookId bookId,
        MemberId memberId,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LoanStatus status,
        LocalDateTime returnedAt) {

    public static Loan issue(LoanId id, BookId bookId, MemberId memberId,
                             LocalDateTime borrowedAt, int loanDays) {
        return new Loan(id, bookId, memberId, borrowedAt,
                borrowedAt.plusDays(loanDays), LoanStatus.ACTIVE, null);
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    public boolean isOverdue(LocalDateTime now) {
        return isActive() && now.isAfter(dueAt);
    }

    public Loan markReturned(LocalDateTime now) {
        if (!isActive()) {
            throw new LibraryException(ErrorCode.LOAN_ALREADY_RETURNED);
        }
        return new Loan(id, bookId, memberId, borrowedAt, dueAt, LoanStatus.RETURNED, now);
    }
}
