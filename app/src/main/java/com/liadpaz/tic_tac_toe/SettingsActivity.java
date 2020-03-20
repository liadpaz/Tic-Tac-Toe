package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.liadpaz.tic_tac_toe.databinding.SettingsActivityBinding;

import java.util.Collections;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    public static final String DARK_MODE_PREFERENCES = "dark_mode";
    public static final String DELETE_PREFERENCE = "delete";
    public static final String ABOUT_PREFERENCE = "about";

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SettingsActivityBinding binding = SettingsActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarSettings);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new SettingsFragment()).commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listener = (sharedPreferences, key) -> {
            if (key.equals(DARK_MODE_PREFERENCES)) {
                if (sharedPreferences.getBoolean(DARK_MODE_PREFERENCES, true)) {
                    if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                        recreate();
                    }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_NO) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        recreate();
                    }
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
            }
        };
    }

    @Override
    public void recreate() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).unregisterOnSharedPreferenceChangeListener(listener);
    }

    static class SettingsFragment extends PreferenceFragmentCompat {

        private static final int LOGIN_ACTIVITY = 162;

        private Preference delete;

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            delete = findPreference(DELETE_PREFERENCE);
            findPreference(ABOUT_PREFERENCE).setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(getContext(), AboutActivity.class));
                return true;
            });

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                delete.setEnabled(false);
            } else {
                delete.setEnabled(true);
            }

            delete.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getContext()).setTitle(R.string.delete_user).setMessage(R.string.delete_user_message).setPositiveButton(R.string.yes, (dialog, which) -> AuthUI.getInstance().delete(getContext()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        delete.setEnabled(false);
                        Toast.makeText(getContext(), R.string.delete_user_success, Toast.LENGTH_LONG).show();
                    } else {
                        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), LOGIN_ACTIVITY);
                    }
                })).setNegativeButton(R.string.no, null).show();
                return true;
            });
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == LOGIN_ACTIVITY && resultCode == RESULT_OK) {
                AuthUI.getInstance().delete(getContext()).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        delete.setEnabled(false);
                        Toast.makeText(getContext(), R.string.delete_user_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), R.string.delete_user_fail, Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), R.string.delete_user_fail, Toast.LENGTH_LONG).show();
            }
        }
    }
}