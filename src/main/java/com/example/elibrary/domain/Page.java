package com.example.elibrary.domain;

import java.util.List;
import java.util.function.Function;

/** 分页值对象：页码从 1 开始，总页数只在这里计算一次。 */
public record Page<T>(List<T> items, int page, int size, long totalElements, int totalPages) {

    public static <T> Page<T> of(List<T> all, int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("page 必须从 1 开始");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size 必须为正数");
        }
        long total = all.size();
        int totalPages = (int) ((total + size - 1) / size);
        int from = (int) Math.min((long) (page - 1) * size, total);
        int to = Math.min(from + size, (int) total);
        return new Page<>(List.copyOf(all.subList(from, to)), page, size, total, totalPages);
    }

    public <R> Page<R> map(Function<T, R> mapper) {
        return new Page<>(items.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
