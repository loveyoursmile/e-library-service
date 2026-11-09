package com.example.elibrary.application;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.BookRepository;
import com.example.elibrary.domain.ErrorCode;
import com.example.elibrary.domain.LibraryException;
import com.example.elibrary.domain.Loan;
import com.example.elibrary.domain.LoanId;
import com.example.elibrary.domain.LoanPolicy;
import com.example.elibrary.domain.LoanRepository;
import com.example.elibrary.domain.MemberId;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 借阅 / 归还 / 查看在借三个用例。
 *
 * <p>只做编排：加载聚合 → 调用领域对象与领域服务 → 保存。所有业务判断都在
 * Book、Loan、LoanPolicy 里，这里没有任何「能不能借」的 if。
 *
 * <p>一次借阅要同时改 Book（库存）与 Loan（新记录），跨聚合一致性由
 * 这一个用例事务 + 进程内锁保证：先算出全部新状态，再依次写入，
 * 校验失败则一个字都不写。
 */
@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;
    private final LoanPolicy loanPolicy;
    private final Clock clock;
    private final ReentrantLock lock = new ReentrantLock();

    public LoanService(BookRepository bookRepository, LoanRepository loanRepository,
                       LoanPolicy loanPolicy, Clock clock) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
        this.loanPolicy = loanPolicy;
        this.clock = clock;
    }

    public LoanView borrow(BookId bookId, MemberId memberId) {
        lock.lock();
        try {
            LocalDateTime now = LocalDateTime.now(clock);
            Book book = requireBook(bookId);
            loanPolicy.checkBorrowable(bookId, loanRepository.findActiveByMember(memberId));

            Book borrowed = book.borrow();
            Loan loan = loanPolicy.issue(loanRepository.nextId(), bookId, memberId, now);
            bookRepository.save(borrowed);
            loanRepository.save(loan);

            log.info("借阅成功: loanId={}, bookId={}, memberId={}", loan.id(), bookId, memberId);
            return LoanView.of(loan, borrowed, now);
        } finally {
            lock.unlock();
        }
    }

    public LoanView returnBook(LoanId loanId, MemberId memberId) {
        lock.lock();
        try {
            LocalDateTime now = LocalDateTime.now(clock);
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new LibraryException(ErrorCode.LOAN_NOT_FOUND));
            if (!loan.memberId().equals(memberId)) {
                throw new LibraryException(ErrorCode.LOAN_ACCESS_DENIED);
            }

            Loan returned = loan.markReturned(now);
            Book restocked = requireBook(loan.bookId()).returnCopy();
            loanRepository.save(returned);
            bookRepository.save(restocked);

            log.info("归还成功: loanId={}, bookId={}, memberId={}", loanId, loan.bookId(), memberId);
            return LoanView.of(returned, restocked, now);
        } finally {
            lock.unlock();
        }
    }

    public List<LoanView> listActive(MemberId memberId) {
        LocalDateTime now = LocalDateTime.now(clock);
        return loanRepository.findActiveByMember(memberId).stream()
                .map(loan -> LoanView.of(loan, requireBook(loan.bookId()), now))
                .toList();
    }

    private Book requireBook(BookId bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new LibraryException(ErrorCode.BOOK_NOT_FOUND));
    }
}
