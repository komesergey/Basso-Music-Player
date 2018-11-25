package com.basso.basso.lastfm;

interface ItemFactory<T> {
    public T createItemFromElement(DomElement element);
}
