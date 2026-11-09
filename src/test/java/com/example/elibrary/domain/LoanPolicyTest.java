package com.example.elibrary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 领域服务：上限、重复借阅、签发借期。 */
class LoanPolicyTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 10, 0);
    private final LoanPolicy policy = new LoanPolicy();

    private List<Loan> activeLoans(int count) {
        List<Loan> loans = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            loans.add(Loan.issue(LoanId.of(i), BookId.of(i), MemberId.of("alice_01"),
                    NOW, LoanPolicy.LOAN_DAYS));
        }
        return loans;
    }

    private void assertRejected(ErrorCode expected, BookId bookId, List<Loan> active) {
        LibraryException ex = assertThrows(LibraryException.class,
                () -> policy.checkBorrowable(bookId, active));
        assertEquals(expected, ex.code());
    }

    @Test
    void allowsBorrowWithinLimits() {
        policy.checkBorrowable(BookId.of(9), activeLoans(2));
    }

    @Test
    void rejectsSixthActiveLoan() {
        assertRejected(ErrorCode.LOAN_LIMIT_EXCEEDED, BookId.of(9), activeLoans(5));
    }

    @Test
    void rejectsBorrowingSameBookTwice() {
        assertRejected(ErrorCode.DUPLICATE_LOAN, BookId.of(1), activeLoans(1));
    }

    @Test
    void issueUsesPolicyLoanDays() {
        Loan loan = policy.issue(LoanId.of(7), BookId.of(3), MemberId.of("bob_02"), NOW);
        assertEquals(NOW.plusDays(14), loan.dueAt());
        assertEquals(BookId.of(3), loan.bookId());
        assertEquals(MemberId.of("bob_02"), loan.memberId());
    }
}
