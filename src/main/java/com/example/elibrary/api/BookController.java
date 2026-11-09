package com.example.elibrary.api;

import com.example.elibrary.api.dto.BookResponse;
import com.example.elibrary.application.BookService;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.Page;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 浏览书籍 / 查询书籍详细资料。HTTP 边界只做裸类型 → 值对象、领域对象 → DTO 的转换。 */
@RestController
@Validated
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/api/books")
    public Page<BookResponse> browse(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") @Positive int page,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return bookService.browse(keyword, page, size).map(BookResponse::from);
    }

    @GetMapping("/api/books/{bookId}")
    public BookResponse detail(@PathVariable @Positive long bookId) {
        return BookResponse.from(bookService.detail(BookId.of(bookId)));
    }
}
