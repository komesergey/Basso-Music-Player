package com.basso.basso.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class WrappedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    public WrappedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D data) {
        if (!isReset()) {
            this.mData = data;
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (this.mData != null) {
            deliverResult(this.mData);
        } else if (takeContentChanged() || this.mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        onStopLoading();
        this.mData = null;
    }
}
