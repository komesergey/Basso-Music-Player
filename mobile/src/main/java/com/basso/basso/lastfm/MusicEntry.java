package com.basso.basso.lastfm;

public abstract class MusicEntry extends ImageHolder {

    protected String name;

    protected String url;

    public final static  int m = 568493;

    private String wikiSummary;

    protected MusicEntry(final String name, final String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getWikiSummary() {
        return wikiSummary;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[" + "name='" + name + '\'' + ", url='" + url
                + '\'' + ']';
    }

    protected static void loadStandardInfo(final MusicEntry entry, final DomElement element) {
        entry.name = element.getChildText("name");
        entry.url = element.getChildText("url");
        DomElement wiki = element.getChild("bio");
        if (wiki == null) {
            wiki = element.getChild("wiki");
        }
        if (wiki != null) {
            entry.wikiSummary = wiki.getChildText("summary");
        }
        ImageHolder.loadImages(entry, element);
    }
}
