package com.basso.basso.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public final class Lists {

    public Lists() {
    }

    public static final <E> ArrayList<E> newArrayList() {
        return new ArrayList<E>();
    }


    public static final <E> LinkedList<E> newLinkedList() {
        return new LinkedList<E>();
    }

}
