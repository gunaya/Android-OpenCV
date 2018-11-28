package com.zucc.androocv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zucc.androocv.UAS.TakePictActivity;
import com.zucc.androocv.UAS.TrackActivity;
import com.zucc.androocv.UTS.HoughActivity;
import com.zucc.androocv.UTS.LoadImageActivity;

public class MainActivity extends AppCompatActivity {
    Button loadBtn, takeBtn, houghBtn, testBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadBtn = findViewById(R.id.btn_load);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intLoad = new Intent(MainActivity.this, LoadImageActivity.class);
                startActivity(intLoad);
            }
        });

        takeBtn = findViewById(R.id.btn_take);
        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intTake = new Intent(MainActivity.this, TakePictActivity.class);
                startActivity(intTake);
            }
        });

        houghBtn = findViewById(R.id.btn_hough);
        houghBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intHough = new Intent(MainActivity.this, HoughActivity.class);
                startActivity(intHough);
            }
        });

        testBtn = findViewById(R.id.btn_test);
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intTest = new Intent(MainActivity.this, TrackActivity.class);
                startActivity(intTest);
            }
        });
    }
}
