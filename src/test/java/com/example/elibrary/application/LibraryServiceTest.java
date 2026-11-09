package com.example.elibrary.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.BookRepository;
import com.example.elibrary.domain.BookType;
import com.example.elibrary.domain.ErrorCode;
import com.example.elibrary.domain.LibraryException;
import com.example.elibrary.domain.LoanId;
import com.example.elibrary.domain.LoanPolicy;
import com.example.elibrary.domain.LoanRepository;
import com.example.elibrary.domain.LoanStatus;
import com.example.elibrary.domain.MemberId;
import com.example.elibrary.domain.Page;
import com.example.elibrary.infrastructure.InMemoryBookRepository;
import com.example.elibrary.infrastructure.InMemoryLoanRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 用例编排测试：不启动 Spring，直接 new 出内存仓储与固定 Clock（AIR：自动、独立、可重复）。
 */
class LibraryServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 9, 10, 0);
    private static final MemberId ALICE = MemberId.of("alice_01");
    private static final MemberId BOB = MemberId.of("bob_02");

    private BookRepository bookRepository;
    private LoanRepository loanRepository;
    private BookService bookService;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        loanRepository = new InMemoryLoanRepository();
        Clock clock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
        bookService = new BookService(bookRepository);
        loanService = new LoanService(bookRepository, loanRepository, new LoanPolicy(), clock);
        bookRepository.save(new Book(BookId.of(1), "重构", "Martin Fowler", BookType.EBOOK, 2, 2));
        bookRepository.save(new Book(BookId.of(2), "设计模式", "Erich Gamma", BookType.EBOOK, 1, 1));
        bookRepository.save(new Book(BookId.of(3), "领域驱动设计", "Eric Evans", BookType.EBOOK, 1, 1));
        bookRepository.save(new Book(BookId.of(4), "整洁架构", "Robert C. Martin", BookType.EBOOK, 1, 1));
        bookRepository.save(new Book(BookId.of(5), "Effective Java", "Joshua Bloch", BookType.EBOOK, 1, 1));
        bookRepository.save(new Book(BookId.of(6), "微服务设计", "Sam Newman", BookType.EBOOK, 1, 1));
        bookRepository.save(new Book(BookId.of(7), "无库存期刊", "ACM", BookType.JOURNAL, 1, 0));
    }

    private ErrorCode codeOf(Runnable action) {
        return assertThrows(LibraryException.class, action::run).code();
    }

    @Test
    void borrowDecrementsInventoryAndCreatesActiveLoan() {
        LoanView view = loanService.borrow(BookId.of(1), ALICE);
        assertEquals(NOW.plusDays(14), view.dueAt());
        assertEquals(LoanStatus.ACTIVE, view.status());
        assertEquals(1, bookService.detail(BookId.of(1)).availableCopies());
        assertEquals(1, loanService.listActive(ALICE).size());
    }

    @Test
    void borrowWhenNoCopiesIsRejected() {
        assertEquals(ErrorCode.BOOK_NOT_AVAILABLE,
                codeOf(() -> loanService.borrow(BookId.of(7), ALICE)));
        assertTrue(loanService.listActive(ALICE).isEmpty());
    }

    @Test
    void borrowUnknownBookIsRejected() {
        assertEquals(ErrorCode.BOOK_NOT_FOUND, codeOf(() -> loanService.borrow(BookId.of(99), ALICE)));
    }

    @Test
    void borrowingSameBookTwiceIsRejected() {
        loanService.borrow(BookId.of(1), ALICE);
        assertEquals(ErrorCode.DUPLICATE_LOAN, codeOf(() -> loanService.borrow(BookId.of(1), ALICE)));
        assertEquals(1, bookService.detail(BookId.of(1)).availableCopies(), "失败不能重复扣库存");
    }

    @Test
    void sixthConcurrentLoanIsRejectedWithoutPartialWrites() {
        for (long id = 1; id <= 5; id++) {
            loanService.borrow(BookId.of(id), ALICE);
        }
        assertEquals(ErrorCode.LOAN_LIMIT_EXCEEDED,
                codeOf(() -> loanService.borrow(BookId.of(6), ALICE)));
        assertEquals(5, loanService.listActive(ALICE).size());
        assertEquals(1, bookService.detail(BookId.of(6)).availableCopies(), "第 6 本不应被扣减");
    }

    @Test
    void limitsArePerMember() {
        for (long id = 1; id <= 5; id++) {
            loanService.borrow(BookId.of(id), ALICE);
        }
        assertTrue(loanService.listActive(BOB).isEmpty(), "上限按读者计算，BOB 初始为空");
        LoanView bobLoan = loanService.borrow(BookId.of(1), BOB);
        assertEquals(BOB, bobLoan.memberId());
        assertEquals(1, loanService.listActive(BOB).size());
    }

    @Test
    void returnRestoresInventoryAndMarksReturned() {
        LoanView borrowed = loanService.borrow(BookId.of(1), ALICE);
        LoanView returned = loanService.returnBook(borrowed.loanId(), ALICE);
        assertEquals(LoanStatus.RETURNED, returned.status());
        assertEquals(2, bookService.detail(BookId.of(1)).availableCopies());
        assertTrue(loanService.listActive(ALICE).isEmpty());
    }

    @Test
    void returningTwiceIsRejected() {
        LoanView borrowed = loanService.borrow(BookId.of(1), ALICE);
        loanService.returnBook(borrowed.loanId(), ALICE);
        assertEquals(ErrorCode.LOAN_ALREADY_RETURNED,
                codeOf(() -> loanService.returnBook(borrowed.loanId(), ALICE)));
        assertEquals(2, bookService.detail(BookId.of(1)).availableCopies(), "库存不能被重复回补");
    }

    @Test
    void returningAnotherMembersLoanIsRejected() {
        LoanView borrowed = loanService.borrow(BookId.of(1), ALICE);
        assertEquals(ErrorCode.LOAN_ACCESS_DENIED,
                codeOf(() -> loanService.returnBook(borrowed.loanId(), BOB)));
        assertEquals(LoanStatus.ACTIVE, loanService.listActive(ALICE).get(0).status());
    }

    @Test
    void returningUnknownLoanIsRejected() {
        assertEquals(ErrorCode.LOAN_NOT_FOUND,
                codeOf(() -> loanService.returnBook(LoanId.of(999), ALICE)));
    }

    @Test
    void listActiveIsScopedToMember() {
        loanService.borrow(BookId.of(1), ALICE);
        loanService.borrow(BookId.of(2), BOB);
        assertEquals(1, loanService.listActive(ALICE).size());
        assertEquals(1, loanService.listActive(BOB).size());
        assertEquals("重构", loanService.listActive(ALICE).get(0).bookTitle());
    }

    @Test
    void browsePaginatesFromOne() {
        Page<Book> first = bookService.browse(null, 1, 3);
        assertEquals(3, first.items().size());
        assertEquals(7, first.totalElements());
        assertEquals(3, first.totalPages());
        assertEquals(1, first.page());
        assertEquals(1, bookService.browse(null, 3, 3).items().size());
    }

    @Test
    void browseFiltersByKeywordCaseInsensitively() {
        Page<Book> result = bookService.browse("java", 1, 10);
        assertEquals(1, result.totalElements());
        assertEquals("Effective Java", result.items().get(0).title());
        assertEquals(3, bookService.browse("设计", 1, 10).totalElements(),
                "关键词按包含匹配：设计模式、领域驱动设计、微服务设计");
    }

    @Test
    void browseRejectsInvalidPaging() {
        assertThrows(IllegalArgumentException.class, () -> bookService.browse(null, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> bookService.browse(null, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> bookService.browse(null, 1, 51));
    }

    @Test
    void detailOfUnknownBookIsRejected() {
        assertEquals(ErrorCode.BOOK_NOT_FOUND, codeOf(() -> bookService.detail(BookId.of(999))));
    }
}
