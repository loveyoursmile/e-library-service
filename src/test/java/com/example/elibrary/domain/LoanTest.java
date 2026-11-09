package com.example.elibrary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

/** 借阅记录聚合：借期、状态单向流转、逾期判定。 */
class LoanTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 10, 0);

    private Loan active() {
        return Loan.issue(LoanId.of(1), BookId.of(1), MemberId.of("alice_01"),
                NOW, LoanPolicy.LOAN_DAYS);
    }

    @Test
    void issueSetsFourteenDayDueDate() {
        Loan loan = active();
        assertEquals(NOW, loan.borrowedAt());
        assertEquals(NOW.plusDays(14), loan.dueAt());
        assertEquals(LoanStatus.ACTIVE, loan.status());
        assertNull(loan.returnedAt());
        assertTrue(loan.isActive());
    }

    @Test
    void markReturnedMovesStatusForward() {
        Loan returned = active().markReturned(NOW.plusDays(2));
        assertEquals(LoanStatus.RETURNED, returned.status());
        assertEquals(NOW.plusDays(2), returned.returnedAt());
        assertFalse(returned.isActive());
        assertTrue(active().isActive(), "原对象不可变");
    }

    @Test
    void returningTwiceIsRejected() {
        LibraryException ex = assertThrows(LibraryException.class,
                () -> active().markReturned(NOW).markReturned(NOW));
        assertEquals(ErrorCode.LOAN_ALREADY_RETURNED, ex.code());
    }

    @Test
    void overdueOnlyWhileActiveAndPastDueDate() {
        assertFalse(active().isOverdue(NOW.plusDays(13)));
        assertTrue(active().isOverdue(NOW.plusDays(15)));
        assertFalse(active().markReturned(NOW.plusDays(15)).isOverdue(NOW.plusDays(20)),
                "已归还的记录不再算逾期");
    }
}
