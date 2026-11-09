package com.example.elibrary.api.dto;

import com.example.elibrary.domain.Book;

/** 对外书籍契约：领域对象不直接序列化，内部改名不影响 JSON。 */
public record BookResponse(
        long id,
        String title,
        String author,
        String type,
        String typeLabel,
        int totalCopies,
        int availableCopies,
        boolean available) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.id().value(), book.title(), book.author(),
                book.type().name(), book.type().label(), book.totalCopies(),
                book.availableCopies(), book.isAvailable());
    }
}
