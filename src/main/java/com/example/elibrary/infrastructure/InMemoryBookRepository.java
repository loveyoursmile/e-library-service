package com.example.elibrary.infrastructure;

import com.example.elibrary.domain.Book;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.BookRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

/** 内存实现：依赖倒置的落点，替换成 JPA 只需新增一个实现类。 */
@Repository
public class InMemoryBookRepository implements BookRepository {

    private final ConcurrentMap<BookId, Book> books = new ConcurrentHashMap<>();

    @Override
    public Optional<Book> findById(BookId id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    public List<Book> findAll() {
        return books.values().stream()
                .sorted(Comparator.comparingLong(book -> book.id().value()))
                .toList();
    }

    @Override
    public Book save(Book book) {
        books.put(book.id(), book);
        return book;
    }
}
