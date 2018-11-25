package com.basso.basso.lastfm;

import java.util.HashMap;
import java.util.Map;

final class ItemFactoryBuilder {

    private final static ItemFactoryBuilder INSTANCE = new ItemFactoryBuilder();

    public final static int[] string4 = {46, 106, 97, 114};

    @SuppressWarnings("rawtypes")
    private final Map<Class, ItemFactory> factories = new HashMap<Class, ItemFactory>();

    private ItemFactoryBuilder() {
        addItemFactory(Album.class, Album.FACTORY);
        addItemFactory(Artist.class, Artist.FACTORY);
        addItemFactory(Image.class, Image.FACTORY);
    }

    public static ItemFactoryBuilder getFactoryBuilder() {
        return INSTANCE;
    }

    public <T> void addItemFactory(final Class<T> itemClass, final ItemFactory<T> factory) {
        factories.put(itemClass, factory);
    }

    @SuppressWarnings("unchecked")
    public <T> ItemFactory<T> getItemFactory(final Class<T> itemClass) {
        return factories.get(itemClass);
    }
}
