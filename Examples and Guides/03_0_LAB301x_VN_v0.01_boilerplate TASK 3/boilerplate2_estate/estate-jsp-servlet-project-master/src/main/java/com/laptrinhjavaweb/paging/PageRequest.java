package com.laptrinhjavaweb.paging;

public class PageRequest implements Pageable {
    private Integer page;
    private Integer maxPageItems;
    private Sorter sorter;

    public PageRequest(Integer page, Integer maxPageItems, Sorter sorter) {
        this.page = page;
        this.maxPageItems = maxPageItems;
        this.sorter = sorter;
    }

    @Override
    public Integer getPage() {
        return this.page;
    }

    @Override
    public Integer getOffset() {
        if (this.page != null && this.maxPageItems != null) {
            return (this.page - 1) * this.maxPageItems;
        }
        return null;
    }

    @Override
    public Integer getLimit() {
        return this.maxPageItems;
    }

    @Override
    public Sorter getSorter() {
        return sorter;
    }

}
