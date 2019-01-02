package com.stparka.kioskapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button kioskmode, settings, statistics, localmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.stparka.kioskapp.R.layout.activity_main);

        kioskmode = findViewById(com.stparka.kioskapp.R.id.button_kioskmode);
        settings = findViewById(com.stparka.kioskapp.R.id.button_settings);
        //statistics = findViewById(com.stparka.kioskapp.R.id.button_statistics);
        localmode = findViewById(com.stparka.kioskapp.R.id.button_localmode);

        kioskmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, KioskActivity.class);
                startActivity(intent);
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);

            }
        });
        /*statistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
                startActivity(intent);

            }
        });*/
        localmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, LocalActivity.class);
                startActivity(intent);

            }
        });


    }
}
