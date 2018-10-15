package com.zucc.androocv;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button loadBtn, takeBtn;

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
    }
}