package com.basso.basso.lastfm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class ImageHolder {

    protected Map<ImageSize, String> imageUrls = new HashMap<ImageSize, String>();

    public Set<ImageSize> availableSizes() {
        return imageUrls.keySet();
    }

    public String getImageURL(final ImageSize size) {
        return imageUrls.get(size);
    }

    protected static void loadImages(final ImageHolder holder, final DomElement element) {
        final Collection<DomElement> images = element.getChildren("image");
        for (final DomElement image : images) {
            final String attribute = image.getAttribute("size");
            ImageSize size = null;
            if (attribute == null) {
                size = ImageSize.UNKNOWN;
            } else {
                try {
                    size = ImageSize.valueOf(attribute.toUpperCase(Locale.ENGLISH));
                } catch (final IllegalArgumentException e) {
                }
            }
            if (size != null) {
                holder.imageUrls.put(size, image.getText());
            }
        }
    }
}
