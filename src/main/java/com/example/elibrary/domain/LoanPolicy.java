package com.example.elibrary.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 领域服务：承载「不属于任何单个实体」的借阅策略——借期、读者在借上限、同书不可重复借。
 *
 * <p>无状态、不依赖 Spring（由启动类注册为 Bean），将来按读者等级换实现即可。
 * 把规则放在这里，应用服务就只需要编排。
 */
public class LoanPolicy {

    public static final int LOAN_DAYS = 14;
    public static final int MAX_ACTIVE_LOANS = 5;

    public void checkBorrowable(BookId bookId, List<Loan> activeLoans) {
        if (activeLoans.size() >= MAX_ACTIVE_LOANS) {
            throw new LibraryException(ErrorCode.LOAN_LIMIT_EXCEEDED);
        }
        if (activeLoans.stream().anyMatch(loan -> loan.bookId().equals(bookId))) {
            throw new LibraryException(ErrorCode.DUPLICATE_LOAN);
        }
    }

    public Loan issue(LoanId id, BookId bookId, MemberId memberId, LocalDateTime now) {
        return Loan.issue(id, bookId, memberId, now, LOAN_DAYS);
    }
}
