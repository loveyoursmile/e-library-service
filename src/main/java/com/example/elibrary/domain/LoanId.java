package com.example.elibrary.domain;

/** 借阅记录标识值对象。 */
public record LoanId(long value) {

    public LoanId {
        if (value <= 0) {
            throw new IllegalArgumentException("loanId 必须为正数");
        }
    }

    public static LoanId of(long value) {
        return new LoanId(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
