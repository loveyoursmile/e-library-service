package com.example.elibrary.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.elibrary.api.dto.BorrowRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** HTTP 契约测试：状态码、Location、分页结构与 ProblemDetail 错误体。 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LibraryApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long borrow(long bookId, String memberId) throws Exception {
        MvcResult result = mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new BorrowRequest(bookId, memberId))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("loanId").asLong();
    }

    @Test
    void browseReturnsPagedBooks() throws Exception {
        mvc.perform(get("/api/books").param("page", "1").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(5))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(9))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[0].typeLabel").value("电子书"));
    }

    @Test
    void browseFiltersByKeyword() throws Exception {
        mvc.perform(get("/api/books").param("keyword", "重构"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].title").value("重构：改善既有代码的设计"));
    }

    @Test
    void browseRejectsTooLargeSize() throws Exception {
        mvc.perform(get("/api/books").param("size", "100"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void bookDetailReturnsBook() throws Exception {
        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("深入理解计算机系统"))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void unknownBookReturns404() throws Exception {
        mvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void borrowReturns201WithLocation() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"memberId\":\"alice_01\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/loans/1"))
                .andExpect(jsonPath("$.loanId").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.bookTitle").value("深入理解计算机系统"));
    }

    @Test
    void borrowWithoutCopiesReturns409() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":2,\"memberId\":\"alice_01\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_AVAILABLE"));
    }

    @Test
    void borrowWithInvalidMemberIdReturns400() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":1,\"memberId\":\"ab\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }

    @Test
    void borrowUnknownBookReturns404() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":999,\"memberId\":\"alice_01\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }

    @Test
    void returnRestocksAndMarksReturned() throws Exception {
        long loanId = borrow(1, "alice_01");
        mvc.perform(post("/api/loans/{loanId}/return", loanId).param("memberId", "alice_01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"))
                .andExpect(jsonPath("$.returnedAt").exists());
        mvc.perform(get("/api/books/1"))
                .andExpect(jsonPath("$.availableCopies").value(3));
        mvc.perform(get("/api/members/alice_01/loans"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void returnTwiceReturns409() throws Exception {
        long loanId = borrow(1, "alice_01");
        mvc.perform(post("/api/loans/{loanId}/return", loanId).param("memberId", "alice_01"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/loans/{loanId}/return", loanId).param("memberId", "alice_01"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LOAN_ALREADY_RETURNED"));
    }

    @Test
    void returnOtherMembersLoanReturns403() throws Exception {
        long loanId = borrow(1, "alice_01");
        mvc.perform(post("/api/loans/{loanId}/return", loanId).param("memberId", "bob_02"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("LOAN_ACCESS_DENIED"));
    }

    @Test
    void activeLoansListsOnlyOwnActive() throws Exception {
        borrow(1, "alice_01");
        borrow(3, "bob_02");
        mvc.perform(get("/api/members/alice_01/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(1))
                .andExpect(jsonPath("$[0].overdue").value(false));
    }

    @Test
    void unknownPathReturns404() throws Exception {
        mvc.perform(get("/api/no-such-endpoint"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"));
    }
}
