package com.liadpaz.tic_tac_toe;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    TextView tv_about_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_about));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        tv_about_version = findViewById(R.id.tv_about_version);
        try {
        tv_about_version.setText(String.format("%s %s", R.string.AppVersion, getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
        } catch (Exception ignored) {
        }

    }
}
