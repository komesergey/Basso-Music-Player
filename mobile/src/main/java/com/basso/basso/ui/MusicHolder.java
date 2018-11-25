package com.basso.basso.ui;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.basso.basso.R;
import java.lang.ref.WeakReference;

public class MusicHolder {

    public WeakReference<RelativeLayout> mOverlay;

    public WeakReference<ImageView> mBackground;

    public WeakReference<ImageView> mImage;

    public WeakReference<TextView> mLineOne;

    public WeakReference<TextView> mLineOneRight;

    public WeakReference<TextView> mLineTwo;

    public WeakReference<TextView> mLineThree;

    public MusicHolder(final View view) {
        super();
        mOverlay = new WeakReference<RelativeLayout>((RelativeLayout)view.findViewById(R.id.image_background));
        mBackground = new WeakReference<ImageView>((ImageView)view.findViewById(R.id.list_item_background));
        mImage = new WeakReference<ImageView>((ImageView)view.findViewById(R.id.image));
        mLineOne = new WeakReference<TextView>((TextView)view.findViewById(R.id.line_one));
        mLineOneRight = new WeakReference<TextView>((TextView)view.findViewById(R.id.line_one_right));
        mLineTwo = new WeakReference<TextView>((TextView)view.findViewById(R.id.line_two));
        mLineThree = new WeakReference<TextView>((TextView)view.findViewById(R.id.line_three));
    }

    public final static class DataHolder {
        public long mItemId;
        public String mLineOne;
        public String mLineOneRight;
        public String mLineTwo;
        public String mLineThree;
        public Bitmap mImage;
        public DataHolder() {
            super();
        }
    }
}
