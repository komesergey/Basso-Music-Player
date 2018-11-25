package com.basso.basso.ui.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.basso.basso.R;
import com.basso.basso.cache.ImageCache;
import com.basso.basso.utils.BassoUtils;
import com.basso.basso.utils.MusicUtils;
import com.basso.basso.utils.PreferenceUtils;
import com.basso.basso.utils.ThemeUtils;

@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    private ImageCache mImageCache;

    private PreferenceUtils mPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        mPreferences = PreferenceUtils.getInstance(this);
        mImageCache = ImageCache.getInstance(this);
        new ThemeUtils(this).themeActionBar(getActionBar(), getString(R.string.menu_settings));
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.action_bar_background)));
        addPreferencesFromResource(R.xml.settings);
        deleteCache();
        try {
            final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            findPreference("version").setSummary(packageInfo.versionName);
        } catch (final NameNotFoundException e) {
            findPreference("version").setSummary("?");
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                finish();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        ViewGroup parent = (ViewGroup)getListView().getParent();
        parent.setPadding(0, 0, 0, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MusicUtils.notifyForegroundStateChanged(this, true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MusicUtils.notifyForegroundStateChanged(this, false);
    }

    private void deleteCache() {
        final Preference deleteCache = findPreference("delete_cache");
        deleteCache.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this).setMessage(R.string.delete_warning)
                        .setPositiveButton(android.R.string.ok, new OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                mImageCache.clearCaches();
                            }
                        }).setNegativeButton(R.string.cancel, new OnClickListener() {

                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        });
    }
}