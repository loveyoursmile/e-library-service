package com.example.elibrary.application;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.Loan;
import com.example.elibrary.domain.LoanId;
import com.example.elibrary.domain.LoanStatus;
import com.example.elibrary.domain.MemberId;
import java.time.LocalDateTime;

/**
 * 借阅用例的读模型：把 Loan 聚合、书名、逾期标记合成一个响应视图，
 * 避免 Web 层再去翻仓储，也避免领域对象被直接序列化成 JSON。
 */
public record LoanView(
        LoanId loanId,
        BookId bookId,
        String bookTitle,
        String bookType,
        MemberId memberId,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LoanStatus status,
        LocalDateTime returnedAt,
        boolean overdue) {

    public static LoanView of(Loan loan, Book book, LocalDateTime now) {
        return new LoanView(loan.id(), loan.bookId(), book.title(), book.type().name(),
                loan.memberId(), loan.borrowedAt(), loan.dueAt(), loan.status(),
                loan.returnedAt(), loan.isOverdue(now));
    }
}
