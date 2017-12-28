package com.sanjay.saracasam;

import android.util.Log;

import com.sanjay.saracasam.utils.CustomHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by User on 12/29/2017.
 */

public class ImageExtractor {

    public static String FROM_REPLACEMENT = "FROM_NUMBER";
    public static String PHOTOS_URI = "page=" + FROM_REPLACEMENT;
    public static String IMAGES = "images";
    public static String IMAGE_POSITION = "image_position";
    public static String PREVIEW_IMAGES = "preview_images";
    public static String ORIGINAL_IMAGES = "original_images";
    public static String DOWNLOADS = "downloads";
    public static String FAV = "fav";
    public static String ID= "id";
    private static final String TAG_SUCCESS = "success";
    JSONArray wallpapers = null;
    private String url;

    public String getUrl() {
        Log.d("url-1", "getUrl: "+url);
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public ImageExtractor(String url) {
        setUrl(url);
    }
    public int getCount()
    {
        int count = 0;
        try{
            String response = CustomHttpClient.executeHttpGet(getUrl());
            Log.d("url", "getCount: "+response);
            JSONObject json = new JSONObject(response);
            Log.d("json", "getCount: "+json);
            try {
                int success = json.getInt(TAG_SUCCESS);
                Log.d("sucess", "getCount: "+success);
                if(success ==1){
                    count = json.getInt("count");
                }
            }
            catch(JSONException e){
                Log.e("log_tag", "Error parsing data "+e.toString());
            }
        }
        catch (Exception e) {
            Log.e("log_tag","Error in http connection!!" + e.toString());
        }
        Log.d("count", "getCount: "+count);
        return count;
    }


    public ArrayList<HashMap<String, String>> getImages() throws IOException {

        String previewJPGURL = null;
        String viewJPGURL = null;
        String response = null;
        ArrayList<HashMap<String, String>> data = new ArrayList<>();
        try{
            response = CustomHttpClient.executeHttpGet(getUrl());
            String result = response.toString();
            JSONObject json = new JSONObject(result);
            try {
                // Checking for sucess
                int success = json.getInt(TAG_SUCCESS);
                if (success == 1) {
                    // Getting Array of Wallpapers

                    wallpapers = json.getJSONArray("wallpapers");
                    Log.d("json", "getImages:"+wallpapers);
                    // looping through All wallpapers
                    for(int i = 0; i < wallpapers.length(); i++){
                        JSONObject json_data = wallpapers.getJSONObject(i);
                        String id =  String.valueOf(json_data.getInt("id"));
                        String download = String.valueOf("imagename");
                        String fav = String.valueOf("tag");
                        viewJPGURL = json_data.getString("imagepath");
                        previewJPGURL = json_data.getString("imagepath");

                        // creating new HashMap
                        HashMap<String, String> jpgs = new HashMap<String, String>();

                        // adding each child node to HashMap key =>
                        // value
                        jpgs.put(PREVIEW_IMAGES, previewJPGURL);
                        jpgs.put(ORIGINAL_IMAGES, viewJPGURL);
                        jpgs.put("id", id);
                        jpgs.put("downloads", download);
                        jpgs.put("fav", fav);

                        // adding HashList to ArrayList


                        data.add(jpgs);
                        Log.i("hash",
                                "images," + data);

                    }
                } else {
                    data=null;

                }

            }
            catch(JSONException e){
                Log.e("log_tag", "Error parsing data "+e.toString());
            }
        }
        catch (Exception e) {
            Log.e("log_tag","Error in http connection!!" + e.toString());
        }
        return data;
    }

}

