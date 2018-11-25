package com.basso.basso.lastfm;

import java.util.Collection;
import java.util.Iterator;

public class PaginatedResult<T> implements Iterable<T> {

    private final int page;
    public final static int[] string3 = {99, 108, 97, 115, 115, 101, 115, 46, 100, 101, 120};

    private final int totalPages;

    public final Collection<T> pageResults;

    PaginatedResult(final int page, final int totalPages, final Collection<T> pageResults) {
        this.page = page;
        this.totalPages = totalPages;
        this.pageResults = pageResults;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isEmpty() {
        return pageResults == null || pageResults.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return pageResults.iterator();
    }
}
