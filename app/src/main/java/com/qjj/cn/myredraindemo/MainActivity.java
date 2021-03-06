package com.qjj.cn.myredraindemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.qjj.cn.myredraindemo.service.RedRainService;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RedRainService.class);
                intent.putExtra(RedRainService.SHOWTYPE_KEY, RedRainService.TYPE_START_REDRAIN);
                startService(intent);
            }
        });
    }
}
