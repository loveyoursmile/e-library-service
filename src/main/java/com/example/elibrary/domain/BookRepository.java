package com.example.elibrary.domain;

import java.util.List;
import java.util.Optional;

/** 书籍仓储端口：只有聚合存取与列举，具体实现由 infrastructure 提供。 */
public interface BookRepository {

    Optional<Book> findById(BookId id);

    List<Book> findAll();

    Book save(Book book);
}
