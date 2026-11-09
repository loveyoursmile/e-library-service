package com.example.elibrary.api;

import com.example.elibrary.api.dto.BorrowRequest;
import com.example.elibrary.api.dto.LoanResponse;
import com.example.elibrary.application.LoanService;
import com.example.elibrary.domain.BookId;
import com.example.elibrary.domain.LoanId;
import com.example.elibrary.domain.MemberId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 借阅、归还与在借查询。没有登录鉴权，归还时用 memberId 显式声明操作者。 */
@RestController
@Validated
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/api/loans")
    public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody BorrowRequest request) {
        LoanResponse body = LoanResponse.from(loanService.borrow(
                BookId.of(request.bookId()), MemberId.of(request.memberId())));
        return ResponseEntity.created(URI.create("/api/loans/" + body.loanId())).body(body);
    }

    @PostMapping("/api/loans/{loanId}/return")
    public LoanResponse returnBook(
            @PathVariable @Positive Long loanId,
            @RequestParam
            @NotBlank
            @Pattern(regexp = MemberId.PATTERN)
            String memberId) {
        return LoanResponse.from(loanService.returnBook(LoanId.of(loanId), MemberId.of(memberId)));
    }

    @GetMapping("/api/members/{memberId}/loans")
    public List<LoanResponse> activeLoans(
            @PathVariable
            @NotBlank
            @Pattern(regexp = MemberId.PATTERN)
            String memberId) {
        return loanService.listActive(MemberId.of(memberId)).stream()
                .map(LoanResponse::from).toList();
    }
}
