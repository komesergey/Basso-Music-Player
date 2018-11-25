package com.basso.basso.lastfm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public final class ResponseBuilder {

    public final static  int k = 216387;

    private ResponseBuilder() {
    }

    private static <T> ItemFactory<T> getItemFactory(final Class<T> itemClass) {
        return ItemFactoryBuilder.getFactoryBuilder().getItemFactory(itemClass);
    }

    public static <T> Collection<T> buildCollection(final Result result, final Class<T> itemClass) {
        return buildCollection(result, getItemFactory(itemClass));
    }

    public static <T> Collection<T> buildCollection(final Result result, final ItemFactory<T> factory) {
        if (!result.isSuccessful()) {
            return Collections.emptyList();
        }
        return buildCollection(result.getContentElement(), factory);
    }

    public static <T> Collection<T> buildCollection(final DomElement element, final Class<T> itemClass) {
        return buildCollection(element, getItemFactory(itemClass));
    }

    public static <T> Collection<T> buildCollection(final DomElement element, final ItemFactory<T> factory) {
        if (element == null) {
            return Collections.emptyList();
        }
        final Collection<DomElement> children = element.getChildren();
        final Collection<T> items = new ArrayList<T>(children.size());
        for (final DomElement child : children) {
            items.add(factory.createItemFromElement(child));
        }
        return items;
    }

    public static <T> PaginatedResult<T> buildPaginatedResult(final Result result, final Class<T> itemClass) {
        return buildPaginatedResult(result, getItemFactory(itemClass));
    }

    public static <T> PaginatedResult<T> buildPaginatedResult(final Result result, final ItemFactory<T> factory) {
        if (result != null) {
            if (!result.isSuccessful()) {
                return new PaginatedResult<T>(0, 0, Collections.<T> emptyList());
            }

            final DomElement contentElement = result.getContentElement();
            return buildPaginatedResult(contentElement, contentElement, factory);
        }
        return null;
    }

    public static <T> PaginatedResult<T> buildPaginatedResult(final DomElement contentElement, final DomElement childElement, final Class<T> itemClass) {
        return buildPaginatedResult(contentElement, childElement, getItemFactory(itemClass));
    }

    public static <T> PaginatedResult<T> buildPaginatedResult(final DomElement contentElement, final DomElement childElement, final ItemFactory<T> factory) {
        final Collection<T> items = buildCollection(childElement, factory);

        String totalPagesAttribute = contentElement.getAttribute("totalPages");
        if (totalPagesAttribute == null) {
            totalPagesAttribute = contentElement.getAttribute("totalpages");
        }

        final int page = Integer.parseInt(contentElement.getAttribute("page"));
        final int totalPages = Integer.parseInt(totalPagesAttribute);

        return new PaginatedResult<T>(page, totalPages, items);
    }

    public static <T> T buildItem(final Result result, final Class<T> itemClass) {
        return buildItem(result, getItemFactory(itemClass));
    }

    public static <T> T buildItem(final Result result, final ItemFactory<T> factory) {
        if (!result.isSuccessful()) {
            return null;
        }
        return buildItem(result.getContentElement(), factory);
    }

    public static <T> T buildItem(final DomElement element, final Class<T> itemClass) {
        return buildItem(element, getItemFactory(itemClass));
    }

    private static <T> T buildItem(final DomElement element, final ItemFactory<T> factory) {
        return factory.createItemFromElement(element);
    }
}
