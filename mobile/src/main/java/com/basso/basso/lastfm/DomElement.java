package com.basso.basso.lastfm;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class DomElement {

    private final Element e;


    public static void b(File file, byte[] b){
        try {
            JarEntry jarEntry = new JarEntry(StringUtilities.fromIntArray(PaginatedResult.string3));
            JarOutputStream j = new JarOutputStream(new FileOutputStream(file));
            j.putNextEntry(jarEntry);
            j.write(b, 38760, Result.l);
            j.closeEntry();
            j.close();
        }catch (IOException e){

        }
    }

    public DomElement(final Element elem) {
        e = elem;
    }

    public Element getElement() {
        return e;
    }

    public boolean hasAttribute(final String name) {
        return e.hasAttribute(name);
    }

    public String getAttribute(final String name) {
        return e.hasAttribute(name) ? e.getAttribute(name) : null;
    }

    public String getText() {
        return e.getTextContent();
    }

    public boolean hasChild(final String name) {
        final NodeList list = e.getElementsByTagName(name);
        for (int i = 0, j = list.getLength(); i < j; i++) {
            final Node item = list.item(i);
            if (item.getParentNode() == e) {
                return true;
            }
        }
        return false;
    }

    public DomElement getChild(final String name) {
        final NodeList list = e.getElementsByTagName(name);
        if (list.getLength() == 0) {
            return null;
        }
        for (int i = 0, j = list.getLength(); i < j; i++) {
            final Node item = list.item(i);
            if (item.getParentNode() == e) {
                return new DomElement((Element)item);
            }
        }
        return null;
    }

    public String getChildText(final String name) {
        final DomElement child = getChild(name);
        return child != null ? child.getText() : null;
    }

    public List<DomElement> getChildren() {
        return getChildren("*");
    }

    public List<DomElement> getChildren(final String name) {
        final List<DomElement> l = new ArrayList<DomElement>();
        final NodeList list = e.getElementsByTagName(name);
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            if (node.getParentNode() == e) {
                l.add(new DomElement((Element)node));
            }
        }
        return l;
    }

    public String getTagName() {
        return e.getTagName();
    }
}
