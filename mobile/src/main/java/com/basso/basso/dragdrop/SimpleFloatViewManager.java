package com.basso.basso.dragdrop;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

public class SimpleFloatViewManager implements DragSortListView.FloatViewManager {

    private final ListView mListView;

    private Bitmap mFloatBitmap;

    private int mFloatBGColor = Color.BLACK;

    public SimpleFloatViewManager(ListView lv) {
        mListView = lv;
    }

    public void setBackgroundColor(int color) {
        mFloatBGColor = color;
    }

    @Override
    public View onCreateFloatView(int position) {
        View v = mListView.getChildAt(position + mListView.getHeaderViewsCount()
                - mListView.getFirstVisiblePosition());

        if (v == null) {
            return null;
        }
        v.setPressed(false);
        v.setDrawingCacheEnabled(true);
        mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        ImageView iv = new ImageView(mListView.getContext());
        iv.setBackgroundColor(mFloatBGColor);
        iv.setPadding(0, 0, 0, 0);
        iv.setImageBitmap(mFloatBitmap);
        return iv;
    }

    @Override
    public void onDestroyFloatView(View floatView) {
        ((ImageView)floatView).setImageDrawable(null);
        mFloatBitmap.recycle();
        mFloatBitmap = null;
    }

    @Override
    public void onDragFloatView(View floatView, Point position, Point touch) {}
}
