package org.teameos.wallpapers.util;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import org.teameos.wallpapers.R;

import java.io.File;
import java.io.FileOutputStream;
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

    public void saveImageToSDCard(Bitmap bitmap, String title) {
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

        if (title == null) {
            LogHelper.d(TAG, "No title found, generating one");
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            title = n + ".png";
        }
        String fname = "Wallpaper-" + title;
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
}