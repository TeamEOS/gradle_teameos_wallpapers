package org.teameos.wallpapers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.teameos.wallpapers.app.AppConst;
import org.teameos.wallpapers.app.AppController;
import org.teameos.wallpapers.picasa.model.Category;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private ProgressBar pbLoader;
    private static final String
            TAG_FEED = "feed",
            TAG_ENTRY = "entry",
            TAG_GPHOTO_ID = "gphoto$id",
            TAG_T = "$t",
            TAG_ALBUM_TITLE = "title";

    private View mLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_splash);
        mLayout = findViewById(R.id.main_content);

        pbLoader = (ProgressBar) findViewById(R.id.pbLoader);
        pbLoader.setVisibility(View.VISIBLE);

        // Picasa request to get list of albums
        String url = AppConst.URL_PICASA_ALBUMS
                .replace("_PICASA_USER_", AppController.getInstance()
                        .getPrefManger().getGoogleUserName());

        Log.d(TAG, "Albums request url: " + url);

        // Preparing volley's json object request
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Method.GET, url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Albums Response: " + response.toString());
                List<Category> albums = new ArrayList<>();
                try {
                    // Parsing the json response
                    JSONArray entry = response.getJSONObject(TAG_FEED)
                            .getJSONArray(TAG_ENTRY);

                    // loop through albums nodes and add them to album
                    // list
                    for (int i = 0; i < entry.length(); i++) {
                        JSONObject albumObj = (JSONObject) entry.get(i);
                        // album id
                        String albumId = albumObj.getJSONObject(
                                TAG_GPHOTO_ID).getString(TAG_T);

                        // album title
                        String albumTitle = albumObj.getJSONObject(
                                TAG_ALBUM_TITLE).getString(TAG_T);

                        Category album = new Category();
                        album.setId(albumId);
                        album.setTitle(albumTitle);

                        // add album to list
                        albums.add(album);

                        Log.d(TAG, "Album Id: " + albumId
                                + ", Album Title: " + albumTitle);
                    }

                    // Store albums in shared pref
                    AppController.getInstance().getPrefManger()
                            .storeCategories(albums);

                    // String the main activity
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    pbLoader.setVisibility(View.GONE);
                    // closing spalsh activity
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(mLayout, getApplicationContext().getString(R.string.msg_unknown_error),
                            Snackbar.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Volley Error: " + error.getMessage());

                View view = findViewById(R.id.main_content);
                // show error snackbar
                Snackbar.make(view, ("Error: " + error.getMessage()),
                        Snackbar.LENGTH_LONG).show();


                // Unable to fetch albums
                // check for existing Albums data in Shared Preferences
                if (AppController.getInstance().getPrefManger()
                        .getCategories() != null && AppController.getInstance().getPrefManger()
                        .getCategories().size() > 0) {
                    // String the main activity
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    // closing spalsh activity
                    finish();
                }
            }
        });

        // disable the cache for this request, so that it always fetches updated
        // json
        jsonObjReq.setShouldCache(false);

        // Making the request
        AppController.getInstance().addToRequestQueue(jsonObjReq);

    }

}
