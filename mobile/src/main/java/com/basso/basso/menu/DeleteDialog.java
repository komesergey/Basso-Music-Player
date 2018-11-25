package com.basso.basso.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.basso.basso.Config;
import com.basso.basso.R;
import com.basso.basso.cache.ImageFetcher;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;

public class DeleteDialog extends DialogFragment {

    public interface DeleteDialogCallback {
        public void onDelete(long[] id);
    }

    private long[] mItemList;

    private ImageFetcher mFetcher;

    public DeleteDialog() {
    }

    public static DeleteDialog newInstance(final String title, final long[] items, final String key) {
        final DeleteDialog frag = new DeleteDialog();
        final Bundle args = new Bundle();
        args.putString(Config.NAME, title);
        args.putLongArray("items", items);
        args.putString("cachekey", key);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String delete = getString(R.string.context_menu_delete);
        final Bundle arguments = getArguments();
        final String key = arguments.getString("cachekey");
        mItemList = arguments.getLongArray("items");
        final String title = arguments.getString(Config.NAME);
        final String dialogTitle = getString(R.string.delete_dialog_title, title);
        mFetcher = BassoUtils.getImageFetcher(getActivity());
        return new AlertDialog.Builder(getActivity()).setTitle(dialogTitle)
                .setMessage(R.string.cannot_be_undone)
                .setPositiveButton(delete, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        mFetcher.removeFromCache(key);
                        MusicUtils.deleteTracks(getActivity(), mItemList);
                        if (getActivity() instanceof DeleteDialogCallback) {
                            ((DeleteDialogCallback)getActivity()).onDelete(mItemList);
                        }
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
