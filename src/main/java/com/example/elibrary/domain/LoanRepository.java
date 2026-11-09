package com.example.elibrary.domain;

import java.util.List;
import java.util.Optional;

/** 借阅记录仓储端口。ID 由存储生成，领域层不 care 具体怎么发号。 */
public interface LoanRepository {

    LoanId nextId();

    Optional<Loan> findById(LoanId id);

    List<Loan> findActiveByMember(MemberId memberId);

    Loan save(Loan loan);
}
