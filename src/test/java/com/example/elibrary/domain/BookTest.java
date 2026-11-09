package com.example.elibrary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** 书籍聚合：库存不变量由构造与行为共同守住。 */
class BookTest {

    private static Book book(int total, int available) {
        return new Book(BookId.of(1), "重构", "Martin Fowler", BookType.EBOOK, total, available);
    }

    @Test
    void borrowDecrementsAvailableCopies() {
        Book borrowed = book(3, 2).borrow();
        assertEquals(1, borrowed.availableCopies());
        assertEquals(3, borrowed.totalCopies(), "总藏量不应随借出变化");
        assertEquals(2, book(3, 2).availableCopies(), "原对象不可变");
    }

    @Test
    void borrowWhenNoCopiesIsRejected() {
        LibraryException ex = assertThrows(LibraryException.class, () -> book(2, 0).borrow());
        assertEquals(ErrorCode.BOOK_NOT_AVAILABLE, ex.code());
    }

    @Test
    void returnCopyIncrementsAvailableCopies() {
        assertEquals(2, book(3, 1).returnCopy().availableCopies());
    }

    @Test
    void returnCopyWhenAlreadyFullSignalsInconsistency() {
        LibraryException ex = assertThrows(LibraryException.class, () -> book(2, 2).returnCopy());
        assertEquals(ErrorCode.INVENTORY_INCONSISTENT, ex.code());
    }

    @Test
    void illegalInventoryIsRejectedAtConstruction() {
        assertThrows(IllegalArgumentException.class, () -> book(2, 3));
        assertThrows(IllegalArgumentException.class, () -> book(-1, -1));
    }

    @Test
    void availabilityReflectsCopies() {
        assertTrue(book(1, 1).isAvailable());
        assertFalse(book(1, 0).isAvailable());
    }
}
