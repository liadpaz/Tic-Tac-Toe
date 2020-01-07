package com.liadpaz.tic_tac_toe;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    TextView tv_about_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar(findViewById(R.id.toolbar_about));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tv_about_version = findViewById(R.id.tv_about_version);
        tv_about_version.setText(String.format("%s %s", getString(R.string.AppVersion), BuildConfig.VERSION_NAME));
    }
}
