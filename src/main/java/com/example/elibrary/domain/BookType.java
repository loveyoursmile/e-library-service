package com.example.elibrary.domain;

/** 数字内容类型，取自需求文档概述：电子书 / 期刊。只影响展示，不影响借还流程。 */
public enum BookType {

    EBOOK("电子书"),
    JOURNAL("期刊");

    private final String label;

    BookType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
