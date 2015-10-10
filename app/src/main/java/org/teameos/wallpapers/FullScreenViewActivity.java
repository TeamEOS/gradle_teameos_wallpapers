package org.teameos.wallpapers;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teameos.wallpapers.app.AppController;
import org.teameos.wallpapers.picasa.model.Wallpaper;
import org.teameos.wallpapers.util.LogHelper;
import org.teameos.wallpapers.util.PermissionHelper;
import org.teameos.wallpapers.util.Utils;

public class FullScreenViewActivity extends Activity
        implements OnClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = FullScreenViewActivity.class
            .getSimpleName();
    public static final String TAG_SEL_IMAGE = "selectedImage";
    private Wallpaper selectedPhoto;
    private ImageView fullImageView;
    private FloatingActionButton fabDownload;
    private FloatingActionButton fabSetWallpaper;
    private Utils utils;
    private ProgressBar pbLoader;

    private static final int REQUEST_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private View mLayout;

    // Picasa JSON response node keys
    private static final String TAG_ENTRY = "entry",
            TAG_MEDIA_GROUP = "media$group",
            TAG_MEDIA_CONTENT = "media$content", TAG_IMG_URL = "url",
            TAG_IMG_WIDTH = "width", TAG_IMG_HEIGHT = "height";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);
        mLayout = findViewById(R.id.main_content);

        fullImageView = (ImageView) findViewById(R.id.imgFullscreen);
        fabSetWallpaper = (FloatingActionButton) findViewById(R.id.fabSetWallpaper);
        fabDownload = (FloatingActionButton) findViewById(R.id.fabDownload);
        pbLoader = (ProgressBar) findViewById(R.id.pbLoader);

        utils = new Utils(getApplicationContext());

        // layout click listeners
        fabSetWallpaper.setOnClickListener(this);
        fabDownload.setOnClickListener(this);

        // setting layout buttons alpha/opacity

        Intent i = getIntent();
        selectedPhoto = (Wallpaper) i.getSerializableExtra(TAG_SEL_IMAGE);

        // check for selected photo null
        if (selectedPhoto != null) {

            // fetch photo full resolution image by making another json request
            fetchFullResolutionImage();

        } else {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.msg_unknown_error), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Fetching image fullresolution json
     */
    private void fetchFullResolutionImage() {
        String url = selectedPhoto.getPhotoJson();

        // show loader before making request
        pbLoader.setVisibility(View.VISIBLE);
        fabSetWallpaper.setVisibility(View.GONE);
        fabDownload.setVisibility(View.GONE);

        // volley's json obj request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                LogHelper.d(TAG,
                        "Image full resolution json: "
                                + response.toString());
                try {
                    // Parsing the json response
                    JSONObject entry = response
                            .getJSONObject(TAG_ENTRY);

                    JSONArray mediacontentArry = entry.getJSONObject(
                            TAG_MEDIA_GROUP).getJSONArray(
                            TAG_MEDIA_CONTENT);

                    JSONObject mediaObj = (JSONObject) mediacontentArry
                            .get(0);

                    String fullResolutionUrl = mediaObj
                            .getString(TAG_IMG_URL);

                    // image full resolution widht and height
                    final int width = mediaObj.getInt(TAG_IMG_WIDTH);
                    final int height = mediaObj.getInt(TAG_IMG_HEIGHT);

                    LogHelper.d(TAG, "Full resolution image. url: "
                            + fullResolutionUrl + ", w: " + width
                            + ", h: " + height);

                    ImageLoader imageLoader = AppController
                            .getInstance().getImageLoader();

                    // We download image into ImageView instead of
                    // NetworkImageView to have callback methods
                    // Currently NetworkImageView doesn't have callback
                    // methods

                    imageLoader.get(fullResolutionUrl,
                            new ImageListener() {

                                @Override
                                public void onErrorResponse(
                                        VolleyError arg0) {
                                    Toast.makeText(
                                            getApplicationContext(),
                                            ("Error: " + arg0.getMessage()),
                                            Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onResponse(
                                        ImageContainer response,
                                        boolean arg1) {
                                    if (response.getBitmap() != null) {
                                        // load bitmap into imageview
                                        fullImageView
                                                .setImageBitmap(response.getBitmap());

                                        LogHelper.d(TAG, "fullImageView width: ", response.getBitmap().getWidth(), " height: ", response.getBitmap().getHeight());

                                        // hide loader and show set &
                                        // download buttons
                                        pbLoader.setVisibility(View.GONE);
                                        fabSetWallpaper
                                                .setVisibility(View.VISIBLE);
                                        fabDownload
                                                .setVisibility(View.VISIBLE);
                                    }
                                }
                            });

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.msg_unknown_error),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                LogHelper.e(TAG, "Error: " , error.getMessage());
                // unable to fetch wallpapers
                // either google username is wrong or
                // devices doesn't have internet connection
                Toast.makeText(getApplicationContext(),
                        ("Error: " + error.getMessage()),
                        Toast.LENGTH_LONG).show();

            }
        });

        // Remove the url from cache
        AppController.getInstance().getRequestQueue().getCache().remove(url);

        // Disable the cache for this url, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }

    /**
     * View click listener
     */
    @Override
    public void onClick(View v) {
        Bitmap bitmap = ((BitmapDrawable) fullImageView.getDrawable())
                .getBitmap();
        switch (v.getId()) {
            // button Download Wallpaper tapped
            case R.id.fabDownload:
                saveImageToSDCard(bitmap);
                break;
            // button Set As Wallpaper tapped
            case R.id.fabSetWallpaper:
                utils.setAsWallpaper(bitmap);
                break;
            default:
                break;
        }
    }

    public void saveImageToSDCard(Bitmap bitmap) {
        // Verify that all required storage permissions have been granted.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Storage permissions have not been granted.
            requestStoragePermissions();
        } else {
            // Storage permissions have been granted. Save wallpaper.
            utils.saveImageToSDCard(bitmap);
        }
    }

    /**
     * Requests the Storage permissions.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestStoragePermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mLayout, R.string.permission_storage_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(FullScreenViewActivity.this, PERMISSIONS_STORAGE,
                                            REQUEST_STORAGE);
                        }
                    })
                    .show();
        } else {
            // Storage permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_STORAGE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_STORAGE) {
            if (PermissionHelper.verifyPermissions(grantResults)) {
                Snackbar.make(mLayout, R.string.permision_available_storage,
                        Snackbar.LENGTH_LONG)
                        .show();
            } else {
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_LONG)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}