package com.example.elibrary.domain;

/** 借阅记录状态：只能 ACTIVE → RETURNED 单向流转。 */
public enum LoanStatus {
    ACTIVE,
    RETURNED
}
