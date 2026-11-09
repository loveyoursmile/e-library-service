package com.example.elibrary.domain;

/** 书籍标识值对象：避免把 bookId 与 loanId 混用，并把「正数」校验收在构造处。 */
public record BookId(long value) {

    public BookId {
        if (value <= 0) {
            throw new IllegalArgumentException("bookId 必须为正数");
        }
    }

    public static BookId of(long value) {
        return new BookId(value);
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}
