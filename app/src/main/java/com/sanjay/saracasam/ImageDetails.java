package com.sanjay.saracasam;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.List;

public class ImageDetails extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private ViewPager pager;

    private DisplayImageOptions options;
    protected ImageLoader imageLoader = ImageLoader.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //getting serialized data from MainActivity
        Bundle bundle = getIntent().getExtras();
        final List<String> imageUrls = bundle.getStringArrayList(ImageExtractor.IMAGES);
        final List<String> downloads = bundle.getStringArrayList(ImageExtractor.DOWNLOADS);
        final List<String> favs = bundle.getStringArrayList(ImageExtractor.FAV);

        int pagerPosition = bundle.getInt(ImageExtractor.IMAGE_POSITION, 0);

        //Setting DisplayImageOptions for UIL
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.stub_image) //setting image to show on loading
                .showImageForEmptyUri(R.drawable.image_for_empty_url)
                .showImageOnFail(R.drawable.image_for_empty_url)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        //initializing viewpager
        pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new ImagePagerAdapter(imageUrls,downloads,favs));
        pager.setCurrentItem(pagerPosition);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    //Adapter for Pager
    private class ImagePagerAdapter extends PagerAdapter {

        private List<String> images;
        private LayoutInflater inflater;
        private List<String> downloads;
        private List<String> favs;

        ImagePagerAdapter(List<String> images,List<String> d,List<String> f) {
            this.images = images;
            this.downloads=d;
            this.favs=f;
            inflater = LayoutInflater.from(ImageDetails.this);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            final View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            final TouchImageView imageView = (TouchImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);
            TextView down = (TextView) imageLayout.findViewById(R.id.downloads);
            TextView fav = (TextView) imageLayout.findViewById(R.id.Fav);

            down.setText(downloads.get(position) + " downloads");
            fav.setText(favs.get(position) + " liked");

            //displaying image using UIL
            imageLoader.displayImage(images.get(position), imageView, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    spinner.setVisibility(View.VISIBLE);
                }
                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String message = null;
                    switch (failReason.getType()) {
                        case IO_ERROR:
                            message = "Input/Output error";
                            break;
                        case DECODING_ERROR:
                            message = "Image can't be decoded";
                            break;
                        case NETWORK_DENIED:
                            message = "Downloads are denied";
                            break;
                        case OUT_OF_MEMORY:
                            message = "Out Of Memory error";
                            break;
                        case UNKNOWN:
                            message = "Unknown error";
                            break;
                    }
                    Toast.makeText(ImageDetails.this, message, Toast.LENGTH_SHORT).show();
                    spinner.setVisibility(View.GONE);
                }
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

                    spinner.setVisibility(View.GONE);

                }
            });
            view.addView(imageLayout, 0);
            return imageLayout;

        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
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
        getMenuInflater().inflate(R.menu.image_details, menu);
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
