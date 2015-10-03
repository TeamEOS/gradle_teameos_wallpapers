package org.teameos.wallpapers.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.google.gson.Gson;

import org.teameos.wallpapers.app.AppConst;
import org.teameos.wallpapers.picasa.model.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PrefManager {
    private static final String TAG = PrefManager.class.getSimpleName();

    // Shared Preferences
    private SharedPreferences pref;

    // Editor for Shared preferences
    private Editor editor;

    // Context
    private Context _context;

    // Shared pref mode
    private int PRIVATE_MODE = 0;

    // Sharedpref file name
    private static final String PREF_NAME = "EOS-Wallpapers";

    // Google's username
    private static final String KEY_GOOGLE_USERNAME = "google_username";

    // No of grid columns
    private static final String KEY_NO_OF_COLUMNS = "no_of_columns";

    // Gallery directory name
    private static final String KEY_GALLERY_NAME = "gallery_name";

    // gallery albums key
    private static final String KEY_ALBUMS = "albums";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);

    }

    public String getGoogleUserName() {
        return pref.getString(KEY_GOOGLE_USERNAME, AppConst.PICASA_USER);
    }

    public int getNoOfGridColumns() {
        return pref.getInt(KEY_NO_OF_COLUMNS, AppConst.NUM_OF_COLUMNS);
    }

    public String getGalleryName() {
        return pref.getString(KEY_GALLERY_NAME, AppConst.SDCARD_DIR_NAME);
    }

    /**
     * Storing albums in shared preferences
     */
    public void storeCategories(List<Category> albums) {
        editor = pref.edit();
        Gson gson = new Gson();

        Log.d(TAG, "Albums: " + gson.toJson(albums));

        editor.putString(KEY_ALBUMS, gson.toJson(albums));

        // save changes
        editor.apply();
    }

    /**
     * Fetching albums from shared preferences. Albums will be sorted before
     * returning in alphabetical order
     */
    public List<Category> getCategories() {
        List<Category> albums;

        if (pref.contains(KEY_ALBUMS)) {
            String json = pref.getString(KEY_ALBUMS, null);
            Gson gson = new Gson();
            Category[] albumArry = gson.fromJson(json, Category[].class);

            albums = Arrays.asList(albumArry);
            albums = new ArrayList<>(albums);
        } else
            return null;

        List<Category> allAlbums = albums;

        // Sort the albums in alphabetical order
        Collections.sort(allAlbums, new Comparator<Category>() {
            public int compare(Category a1, Category a2) {
                return a1.getTitle().compareToIgnoreCase(a2.getTitle());
            }
        });

        return allAlbums;

    }

    /**
     * Comparing albums titles for sorting
     */
    private class CustomComparator implements Comparator<Category> {
        @Override
        public int compare(Category c1, Category c2) {
            return c1.getTitle().compareTo(c2.getTitle());
        }
    }
}