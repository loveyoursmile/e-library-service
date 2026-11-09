package com.example.elibrary.domain;

/** 领域层唯一异常类型：错误语义由 ErrorCode 表达，HTTP 映射留给 api 层。 */
public class LibraryException extends RuntimeException {

    private final ErrorCode code;

    public LibraryException(ErrorCode code) {
        this(code, code.message());
    }

    public LibraryException(ErrorCode code, String detail) {
        super(detail);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
