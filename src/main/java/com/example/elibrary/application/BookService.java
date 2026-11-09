package com.example.elibrary.application;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.BookRepository;
import com.example.elibrary.domain.ErrorCode;
import com.example.elibrary.domain.LibraryException;
import com.example.elibrary.domain.Page;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** 浏览与详情两个只读用例。关键词过滤与分页都在这里，仓储只负责存取。 */
@Service
public class BookService {

    public static final int MAX_PAGE_SIZE = 50;

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Page<Book> browse(String keyword, int page, int size) {
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size 不能超过 " + MAX_PAGE_SIZE);
        }
        String normalized = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        List<Book> matched = bookRepository.findAll().stream()
                .filter(book -> normalized.isEmpty()
                        || contains(book.title(), normalized)
                        || contains(book.author(), normalized))
                .toList();
        return Page.of(matched, page, size);
    }

    public Book detail(BookId id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new LibraryException(ErrorCode.BOOK_NOT_FOUND));
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
