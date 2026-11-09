package com.example.elibrary.infrastructure;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.BookRepository;
import com.example.elibrary.domain.BookType;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 启动注入示例书目，开箱即用。第 2 本故意设为 0 可借，方便直接验证 409。
 * 生产环境应换成迁移脚本或管理端录入。
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (!bookRepository.findAll().isEmpty()) {
            return;
        }
        List.of(
                new Book(BookId.of(1), "深入理解计算机系统", "Randal E. Bryant", BookType.EBOOK, 3, 3),
                new Book(BookId.of(2), "设计模式", "Erich Gamma", BookType.EBOOK, 2, 0),
                new Book(BookId.of(3), "重构：改善既有代码的设计", "Martin Fowler", BookType.EBOOK, 2, 2),
                new Book(BookId.of(4), "领域驱动设计", "Eric Evans", BookType.EBOOK, 1, 1),
                new Book(BookId.of(5), "整洁架构", "Robert C. Martin", BookType.EBOOK, 2, 2),
                new Book(BookId.of(6), "软件工程学报·2026 年第 3 期", "中国计算机学会", BookType.JOURNAL, 4, 4),
                new Book(BookId.of(7), "ACM Computing Surveys Vol.58", "ACM", BookType.JOURNAL, 2, 2),
                new Book(BookId.of(8), "Effective Java", "Joshua Bloch", BookType.EBOOK, 3, 3),
                new Book(BookId.of(9), "微服务设计", "Sam Newman", BookType.EBOOK, 2, 2)
        ).forEach(bookRepository::save);
    }
}
