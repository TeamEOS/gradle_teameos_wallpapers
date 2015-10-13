package org.teameos.wallpapers.util;

import android.app.Activity;
import android.app.Application;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.teameos.wallpapers.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

public class Utils {
    private String TAG = Utils.class.getSimpleName();
    private Context _context;
    private PrefManager pref;

    // constructor
    public Utils(Context context) {
        this._context = context;
        pref = new PrefManager(_context);
    }

    /*
     * getting screen width
     */
    @SuppressWarnings("deprecation")
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) _context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) {
            // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }

    public void saveImageToSDCard(Bitmap bitmap) {
        File myDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                pref.getGalleryName());
        if (!myDir.exists()) {
            boolean mkdirs = myDir.mkdirs();
            if (!mkdirs) {
                throw new RuntimeException("Cannot create folder" + myDir.getAbsolutePath());
            }
        }

        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Wallpaper-" + n + ".png";
        File file = new File(myDir, fname);
        if (file.exists()) {
            boolean fileDel = file.delete();
            if (!fileDel) {
                throw new RuntimeException("Cannot delete file" + file.getAbsolutePath());
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Toast.makeText(
                    _context,
                    _context.getString(R.string.toast_saved).replace("#",
                            "\"" + pref.getGalleryName() + "\""),
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Wallpaper saved to: " + file.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(_context,
                    _context.getString(R.string.toast_saved_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void setAsWallpaper(Bitmap bitmap) {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(_context);
            wm.setBitmap(bitmap);
            Toast.makeText(_context,
                    _context.getString(R.string.toast_wallpaper_set),
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(_context,
                    _context.getString(R.string.toast_wallpaper_set_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private int getWidth(Activity activity) {
        WallpaperManager wallpaperManager
                = WallpaperManager.getInstance(activity);
        try {
            return wallpaperManager.getDesiredMinimumWidth();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getHeight(Activity activity) {
        WallpaperManager wallpaperManager
                = WallpaperManager.getInstance(activity);
        try {
            return wallpaperManager.getDesiredMinimumHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(_context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}