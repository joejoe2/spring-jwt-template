package com.joejoe2.demo.data;

import lombok.Data;

import java.util.List;

@Data
public class PageList<E> {
    private long totalItems;
    private int currentPage;
    private int totalPages;
    private int pageSize;
    private List<E> list;

    public PageList(long totalItems, int currentPage, int totalPages, int pageSize, List<E> list) {
        this.totalItems = totalItems;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.list = list;
    }
}
