package com.example.elibrary.domain;

import java.util.Objects;

/**
 * 书籍聚合根：书目信息 + 馆藏库存。
 *
 * <p>不可变。库存不变量 {@code 0 <= availableCopies <= totalCopies} 由构造与
 * borrow/returnCopy 两个行为共同守住，外部拿不到可写的库存字段。
 */
public record Book(
        BookId id,
        String title,
        String author,
        BookType type,
        int totalCopies,
        int availableCopies) {

    public Book {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(author, "author");
        Objects.requireNonNull(type, "type");
        if (totalCopies < 0 || availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("馆藏库存不合法");
        }
    }

    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /** 借出一本；无可借副本时抛业务异常，不返回「负数库存」这种非法状态。 */
    public Book borrow() {
        if (!isAvailable()) {
            throw new LibraryException(ErrorCode.BOOK_NOT_AVAILABLE);
        }
        return new Book(id, title, author, type, totalCopies, availableCopies - 1);
    }

    /** 归还一本；已满说明状态被破坏，属于数据不一致而不是普通业务冲突。 */
    public Book returnCopy() {
        if (availableCopies >= totalCopies) {
            throw new LibraryException(ErrorCode.INVENTORY_INCONSISTENT);
        }
        return new Book(id, title, author, type, totalCopies, availableCopies + 1);
    }
}
