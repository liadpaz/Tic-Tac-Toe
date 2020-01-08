package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OptionsActivity extends AppCompatActivity {

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listener = (sharedPreferences, key) -> {
            if (key.equals("dark_mode")) {
                String mode;
                if ((mode = sharedPreferences.getString("dark_mode", "")).equals("on")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else if (mode.equals("off")) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                }
                recreate();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(OptionsActivity.this).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(OptionsActivity.this).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        Preference delete;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            delete = Objects.requireNonNull(findPreference("delete"));

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                delete.setEnabled(false);
            } else {
                delete.setEnabled(true);
            }

            delete.setOnPreferenceClickListener(preference -> {
                AuthUI.getInstance().delete(Objects.requireNonNull(getContext())).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        delete.setEnabled(false);
                        Toast.makeText(getContext(), R.string.delete_user_success, Toast.LENGTH_LONG).show();
                    } else {
                        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(), 1);
                    }
                });
                return true;
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == 1 && resultCode == RESULT_OK) {
                AuthUI.getInstance().delete(Objects.requireNonNull(getContext())).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        delete.setEnabled(false);
                        Toast.makeText(getContext(), R.string.delete_user_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), R.string.delete_user_fail, Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}