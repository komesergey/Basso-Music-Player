package com.basso.basso.menu;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.basso.basso.R;
import com.basso.basso.utils.MusicUtils;

public abstract class BasePlaylistDialog extends DialogFragment {

    protected AlertDialog mPlaylistDialog;

    protected EditText mPlaylist;

    protected Button mSaveButton;

    protected String mPrompt;

    protected String mDefaultname;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        mPlaylistDialog = new AlertDialog.Builder(getActivity()).create();
        mPlaylist = new EditText(getActivity());
        mPlaylist.setSingleLine(true);
        mPlaylist.setInputType(mPlaylist.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        mPlaylistDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.save),
                new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        onSaveClick();
                        MusicUtils.refresh();
                        dialog.dismiss();
                    }
                });
        mPlaylistDialog.setButton(Dialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        closeKeyboard();
                        MusicUtils.refresh();
                        dialog.dismiss();
                    }
                });

        mPlaylist.post(new Runnable() {

            @Override
            public void run() {
                openKeyboard();
                mPlaylist.requestFocus();
                mPlaylist.selectAll();
            };
        });

        initObjects(savedInstanceState);
        mPlaylistDialog.setTitle(mPrompt);
        mPlaylistDialog.setView(mPlaylist);
        mPlaylist.setText(mDefaultname);
        mPlaylist.setSelection(mDefaultname.length());
        mPlaylist.addTextChangedListener(mTextWatcher);
        mPlaylistDialog.show();
        return mPlaylistDialog;
    }

    protected void openKeyboard() {
        final InputMethodManager mInputMethodManager = (InputMethodManager)getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.toggleSoftInputFromWindow(mPlaylist.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, 0);
    }

    protected void closeKeyboard() {
        final InputMethodManager mInputMethodManager = (InputMethodManager)getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputMethodManager.hideSoftInputFromWindow(mPlaylist.getWindowToken(), 0);
    }

    private final TextWatcher mTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before,
                final int count) {
            onTextChangedListener();
        }

        @Override
        public void afterTextChanged(final Editable s) {
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                final int after) {
        }
    };

    public abstract void initObjects(Bundle savedInstanceState);

    public abstract void onSaveClick();

    public abstract void onTextChangedListener();
}
