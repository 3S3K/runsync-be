package com._s3k.runsync.global.common;

import java.util.List;

public class ScrollPaginationCollection<T> {

    private final List<T> contents;
    private final int requestSize;

    private ScrollPaginationCollection(List<T> contents, int requestSize) {
        this.contents = contents;
        this.requestSize = requestSize;
    }

    public static <T> ScrollPaginationCollection<T> of(List<T> contents, int requestSize) {
        return new ScrollPaginationCollection<>(contents, requestSize);
    }

    public boolean hasNext() {
        return contents.size() > requestSize;
    }

    public List<T> getCurrentPageContents() {
        if (hasNext()) {
            return contents.subList(0, requestSize);
        }
        return contents;
    }
}
