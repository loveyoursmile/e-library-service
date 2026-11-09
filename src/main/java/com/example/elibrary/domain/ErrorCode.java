package com.example.elibrary.domain;

/**
 * 业务错误码。
 *
 * <p>只保存 HTTP 状态数值而不引用 Spring 类型，领域层仍然不依赖 Web 框架；
 * 错误体统一由 api 层的 ApiExceptionHandler 生成。
 */
public enum ErrorCode {

    INVALID_ARGUMENT(400, "请求参数不合法"),
    BOOK_NOT_FOUND(404, "书籍不存在"),
    LOAN_NOT_FOUND(404, "借阅记录不存在"),
    RESOURCE_NOT_FOUND(404, "请求的接口不存在"),
    LOAN_ACCESS_DENIED(403, "无权操作他人的借阅记录"),
    BOOK_NOT_AVAILABLE(409, "该书当前无可借副本"),
    DUPLICATE_LOAN(409, "已借阅该书且尚未归还"),
    LOAN_LIMIT_EXCEEDED(409, "同时借阅数量已达上限"),
    LOAN_ALREADY_RETURNED(409, "该借阅记录已归还"),
    INVENTORY_INCONSISTENT(500, "库存数据不一致"),
    INTERNAL_ERROR(500, "服务内部错误");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int status() {
        return status;
    }

    public String message() {
        return message;
    }
}
