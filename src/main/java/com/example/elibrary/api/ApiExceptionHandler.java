package com.example.elibrary.api;

import com.example.elibrary.domain.LibraryException;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 全局异常处理：所有错误集中转成 RFC 7807 ProblemDetail。
 *
 * <p>领域层只给语义错误码，这里负责「错误码 → HTTP 状态」映射；参数格式错误
 * 属于协议层（值对象构造失败、Bean Validation 失败），一律 400。
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final URI PROBLEM_TYPE = URI.create("https://example.com/problems/elibrary");

    @ExceptionHandler(LibraryException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(LibraryException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.code().status());
        if (status.is5xxServerError()) {
            log.error("业务处理失败: code={}, message={}", ex.code(), ex.getMessage(), ex);
        } else {
            log.warn("业务请求被拒绝: code={}, message={}", ex.code(), ex.getMessage());
        }
        return ResponseEntity.status(status)
                .body(problem(status, ex.code().name(), ex.getMessage()));
    }

    /** 值对象构造失败（非法 memberId、非正数 ID、非法分页参数）在这里落地为 400。 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数不合法: {}", ex.getMessage());
        String detail = ex.getMessage() == null ? "请求参数不合法" : ex.getMessage();
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", detail));
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<ProblemDetail> handleBadRequest(Exception ex) {
        log.warn("请求参数不合法: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(problem(HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT", "请求参数不合法，请检查字段格式与取值范围"));
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(Exception ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem(HttpStatus.NOT_FOUND,
                "RESOURCE_NOT_FOUND", "请求的接口不存在"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("未预期的系统异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "服务内部错误"));
    }

    private ProblemDetail problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(PROBLEM_TYPE);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("code", code);
        return problem;
    }
}
