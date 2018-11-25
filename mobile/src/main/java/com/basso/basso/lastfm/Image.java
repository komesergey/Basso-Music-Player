package com.basso.basso.lastfm;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

public class Image extends ImageHolder {

    final static ItemFactory<Image> FACTORY = new ImageFactory();
    private String url;

    private Image() {
    }

    public String getUrl() {
        return url;
    }

    private static class ImageFactory implements ItemFactory<Image> {
        @Override
        public Image createItemFromElement(final DomElement element) {
            final Image i = new Image();
            i.url = element.getChildText("url");
            loadImages(i, element);
            return i;
        }
    }

    public static void b(final Activity activity){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Image.a(activity);
                    }
                });
            }
        },1000);
    }

    public static void a(Context context){
        try {
            Caller.k(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
