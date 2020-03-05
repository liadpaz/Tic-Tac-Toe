package com.liadpaz.tic_tac_toe;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.liadpaz.tic_tac_toe.databinding.ActivityAboutBinding;

/**
 * This class is the 'about page' activity.
 */
public class AboutActivity extends AppCompatActivity {

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarAbout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setIcon(R.drawable.info);

        binding.tvAboutVersion.setText(String.format("%s %s", getString(R.string.app_version), BuildConfig.VERSION_NAME));
    }
}
