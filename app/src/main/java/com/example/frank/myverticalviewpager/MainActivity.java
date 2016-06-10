package com.example.frank.myverticalviewpager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @Bind(R.id.verticalviewpager)
    MyVerticleViewPager mVerticalviewpager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mVerticalviewpager.setOnPageChangeListener(new MyVerticleViewPager.OnPageChangeListener() {
            @Override
            public void onPageChange(int page) {
                Toast.makeText(MainActivity.this, "currentpage=" + page, Toast.LENGTH_SHORT).show();
            }
        });

    }


}
