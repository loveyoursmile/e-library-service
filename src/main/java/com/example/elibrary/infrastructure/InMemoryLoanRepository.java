package com.example.elibrary.infrastructure;

import com.example.elibrary.domain.Loan;
import com.example.elibrary.domain.LoanId;
import com.example.elibrary.domain.LoanRepository;
import com.example.elibrary.domain.MemberId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** 内存实现：用 AtomicLong 发号，单实例下不会重复。 */
@Repository
public class InMemoryLoanRepository implements LoanRepository {

    private final ConcurrentMap<LoanId, Loan> loans = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    @Override
    public LoanId nextId() {
        return LoanId.of(sequence.incrementAndGet());
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        return Optional.ofNullable(loans.get(id));
    }

    @Override
    public List<Loan> findActiveByMember(MemberId memberId) {
        return loans.values().stream()
                .filter(Loan::isActive)
                .filter(loan -> loan.memberId().equals(memberId))
                .sorted(Comparator.comparingLong(loan -> loan.id().value()))
                .toList();
    }

    @Override
    public Loan save(Loan loan) {
        loans.put(loan.id(), loan);
        return loan;
    }
}
