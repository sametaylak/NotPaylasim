package com.sametaylak.notpaylasim;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoViewAttacher;

public class FullscreenActivity extends AppCompatActivity {

    private String GLOBAL_URL = "http://www.whatsapping.org/";
    private float x1,x2;
    static final int MIN_DISTANCE = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_fullscreen);

        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);

        /*final PhotoViewAttacher mAttacher;

        ImageView imageView = (ImageView) findViewById(R.id.fullImage);
        */
        Bundle extras = getIntent().getExtras();

        ArrayList<String> gelenler = extras.getStringArrayList("url");
        int indis = extras.getInt("indis");

        FullAdapter adapter = new FullAdapter(this, gelenler);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(indis);
        /*

        assert gelenler != null;
        assert imageView != null;

        mAttacher = new PhotoViewAttacher(imageView);

        Picasso.with(this).load(GLOBAL_URL + gelenler.get(indis))
                .into(imageView, new com.squareup.picasso.Callback() {

                    @Override
                    public void onSuccess() {
                        mAttacher.update();
                    }

                    @Override
                    public void onError() {

                    }
                });

        System.out.println(gelenler);*/

    }

}
