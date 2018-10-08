package com.sanjay.saracasam;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.sanjay.saracasam.utils.ConnectionDetector;

import android.widget.AbsListView.OnScrollListener;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<String> imageUrls;
    private List<String> previewImageUrls;
    private List<String> downloads;
    private List<String> fav;
    private List<String> ids;
    private TextView noConnectTextView;
    private DisplayImageOptions options;
    private ImageAdapter imageAdapter;
    private GridView gridView;
    private TextView results;
    private DownloadPreviewImagesTask downloadPreviewImagesTask;
    private int page = 0;
    int count=0;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private String gallery_url="http://13.126.191.69/api/saracasam/";
    ConnectionDetector cd;
    Boolean isInternetPresent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//
        noConnectTextView = (TextView) findViewById(R.id.no_connect_message);

        cd = new ConnectionDetector(this);

        results = (TextView) findViewById(R.id.tresults);

        isInternetPresent = cd.isConnectingToInternet();

        // check for Internet status
        if (isInternetPresent) {
            imageUrls = new ArrayList<String>();
            previewImageUrls = new ArrayList<String>();
            downloads = new ArrayList<String>();
            fav = new ArrayList<String>();
            ids = new ArrayList<String>();

            //initializing gridview
            initGridView();

            //executing async task for getting data
            downloadPreviewImagesTask = getDownloadPreviewImagesTask(R.id.preview_img_loading, geturl());
            if (downloadPreviewImagesTask != null && downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
                downloadPreviewImagesTask.attachActivity(this);
                downloadPreviewImagesTask.setSpinnerVisible();

            } else {
                Log.d("downloadPreview", "downloadPreviewImagesTask has executed already");
            }
        }
        else
        {
            showAlertDialog(MainActivity.this, "No Internet Connection",
                    "You don't have internet connection!", false);
        }
    }
    public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        // Setting Dialog Title
        alertDialog.setTitle(title);
        // Setting Dialog Message
        alertDialog.setMessage(message);
        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }
    public String geturl(){

        //You can set various URLs for different types of query here!
        Log.i("url", "geturl:"+gallery_url);
        return gallery_url;

    }
    private DownloadPreviewImagesTask getDownloadPreviewImagesTask(int spinnerID, String url) {
        downloadPreviewImagesTask = new DownloadPreviewImagesTask();
        downloadPreviewImagesTask.setSpinner(spinnerID);
        downloadPreviewImagesTask.attachActivity(this);
        downloadPreviewImagesTask.execute(url); // show spinner
        return downloadPreviewImagesTask;
    }
    class EndlessListListener implements OnScrollListener {

        private int visibleThreshold = 5;  //setting visible threshold for loading new data
        private int previousTotal = 0;
        private boolean loading = true;

        public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
            if (loading) {
                if (totalCount > previousTotal) {
                    loading = false;
                    previousTotal = totalCount;
                }
            }
            Log.i("check", String.valueOf(totalCount) + " " + String.valueOf(visibleCount) + " " + String.valueOf(firstVisible));
            if (!loading && (totalCount - visibleCount) <= (firstVisible + visibleThreshold)) {
                Log.d("loadData", "new data loaded");
                String photosUri = ImageExtractor.PHOTOS_URI;
                String url = geturl() + photosUri.replace(ImageExtractor.FROM_REPLACEMENT, String.valueOf(page));
                if (downloadPreviewImagesTask != null && downloadPreviewImagesTask.getStatus() == AsyncTask.Status.RUNNING) {
                    Log.d("downloadPreview", "downloadPreviewImagesTask is running");
                } else if (page < (count / 21)) //checking if there is more data
                {
                    downloadPreviewImagesTask = getDownloadPreviewImagesTask(R.id.new_photos_loading, url);
                    Log.i("url", url);
                    page = page + 1;
                }
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    }
    //Async Task for downloading new data
    class DownloadPreviewImagesTask extends AsyncTask<String, Void, ArrayList<HashMap<String, String>>> {

        private Exception exc = null;
        private ProgressBar spinner;
        private MainActivity imageGridActivity = null;
        private int spinnerId;

        public AsyncTask<String, Void, ArrayList<HashMap<String, String>>> setSpinner(int spinnerId) {
            this.spinnerId = spinnerId;
            return this;
        }

        public void attachActivity(MainActivity imageGridActivity) {
            this.imageGridActivity = imageGridActivity;
        }

        public void detachActivity() {
            this.imageGridActivity = null;
        }

        public ProgressBar getSpinner() {
            return spinner;
        }

        @Override
        protected ArrayList<HashMap<String, String>> doInBackground(String... params) {
            String url = "";
            if( params.length > 0 ){
                url = params[0];
            }
            ImageExtractor imageExtractor = new ImageExtractor(url);
            ArrayList<HashMap<String, String>> images = null;
            try {
                images = imageExtractor.getImages();
                count = imageExtractor.getCount();

            } catch (IOException e) {
                exc = e;
                Log.e("catched_error", e.getMessage(), e);
            }
            return images;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setSpinnerVisible();
        }

        public void setSpinnerVisible() {
            spinner = (ProgressBar) imageGridActivity.findViewById(spinnerId);
            spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(ArrayList<HashMap<String, String>> images) {
            super.onPostExecute(images);
            spinner.setVisibility(View.GONE);
            if (exc != null) {
                imageGridActivity.showError(exc);
            } else if (images !=null) {
                imageGridActivity.initOriginalAndPreviewUrls(images);

                if (gridView == null && imageAdapter == null) {
                    initGridView();
                } else
                    imageGridActivity.getImageAdapter().notifyDataSetChanged();
            }
            else
            {
                results.setText("Sorry! No Results Found!");
            }
        }
    }
    public ImageAdapter getImageAdapter() {
        return imageAdapter;
    }

    private void showError(Exception exc) {
        noConnectTextView.setText(exc.getMessage());
        noConnectTextView.setVisibility(View.VISIBLE);
    }
    private void initOriginalAndPreviewUrls(ArrayList<HashMap<String, String>> images) {

        for(int i=0;i<=images.size()-1;i++){

            String vurls= images.get(i).get(ImageExtractor.ORIGINAL_IMAGES);
            String purls= images.get(i).get(ImageExtractor.PREVIEW_IMAGES);
            String download= images.get(i).get("downloads");
            String ifav= images.get(i).get("fav");
            String ide= images.get(i).get("id");
            imageUrls.add(vurls);
            previewImageUrls.add(purls);
            downloads.add(download);
            fav.add(ifav);
            ids.add(ide);
        }

    }
    private void startImageGalleryActivity(int position) {
        Intent intent = new Intent(this, ImageDetails.class);
        intent.putExtra(ImageExtractor.IMAGES, (Serializable)imageUrls);
        intent.putExtra(ImageExtractor.PREVIEW_IMAGES, (Serializable)previewImageUrls);
        intent.putExtra(ImageExtractor.DOWNLOADS,(Serializable) downloads);
        intent.putExtra(ImageExtractor.FAV, (Serializable) fav);
        intent.putExtra(ImageExtractor.IMAGE_POSITION, position);
        intent.putExtra(ImageExtractor.ID, (Serializable) ids);

        startActivity(intent);
    }
    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridview);
        imageAdapter = new ImageAdapter();
        gridView.setAdapter(imageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startImageGalleryActivity(position);
            }
        });
        gridView.setOnScrollListener(new EndlessListListener());
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.spinner)
                .showImageForEmptyUri(R.drawable.noimage)
                .showImageOnFail(R.drawable.broken)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    public class ImageAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return imageUrls.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) getLayoutInflater().inflate(R.layout.item_grid_image, parent, false);
            } else {
                imageView = (ImageView) convertView;
            }

            imageLoader.displayImage(previewImageUrls.get(position), imageView, options);

            return imageView;
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
