package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int USER_AUTH = 578;
    public static final int SETTINGS_ACTIVITY = 275;

    FirebaseAuth auth;
    DatabaseReference mainRef;

    boolean stats;

    TextView tv_title;
    TextView tv_user_state;

    Button btn_singleplayer;
    Button btn_multiplayer;
    Button btn_user_action;

    CountDownTimer count;

    SharedPreferences sharedPreferences;

    int devCounter;

    static boolean first_open = true;
    boolean can_open_menu = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar_main));
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        boolean mode = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).getBoolean(SettingsActivity.DARK_MODE_PREFERENCES, true);
        if (mode && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        } else if (!mode && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }

        new Stats(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        sharedPreferences = getSharedPreferences("name", 0);
        auth = FirebaseAuth.getInstance();

        tv_title = findViewById(R.id.tv_title);
        tv_user_state = findViewById(R.id.tv_user_state);
        btn_singleplayer = findViewById(R.id.btn_singleplayer);
        btn_multiplayer = findViewById(R.id.btn_multiplayer);
        btn_user_action = findViewById(R.id.btn_user_action);

        count = new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                devCounter = 0;
            }
        };

        tv_title.setOnClickListener(v -> {
            count.cancel();
            devCounter++;
            if (devCounter != 5) {
                Toast.makeText(MainActivity.this, String.format("%s %s %s", MainActivity.this.getString(R.string.dev_first), String.valueOf(5 - devCounter), MainActivity.this.getString(R.string.dev_second)), Toast.LENGTH_SHORT).show();
                count.start();
            } else {
                devCounter = 0;
                if (auth.getCurrentUser() != null) {
                    if (Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), "liadpazhamud@gmail.com")) {
                        MainActivity.this.startActivity(new Intent(MainActivity.this, DeveloperActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, R.string.not_authorized, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.not_authorized, Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_singleplayer.setOnClickListener(v -> MainActivity.this.startActivity(new Intent(MainActivity.this, OptionsActivity.class)));
        btn_multiplayer.setOnClickListener(v -> new InternetTask(MainActivity.this).execute());
        btn_user_action.setOnClickListener(v -> {
            if (auth.getCurrentUser() != null) {    //User connected (logout active)
                AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setUsername(null);
                        Firebase.userRef = null;
                        btn_user_action.setText(R.string.login);
                        tv_user_state.setText(String.format("%s", getString(R.string.not_connected)));
                        Toast.makeText(MainActivity.this, R.string.logout_message, Toast.LENGTH_LONG).show();
                    }
                });
            } else {    //No user connected (login active)
                List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), USER_AUTH);
            }
        });

        Utils.setTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If the user is authenticated
        if (auth.getCurrentUser() != null) {
            btn_user_action.setText(R.string.logout);
            btn_singleplayer.setEnabled(false);
            btn_multiplayer.setEnabled(false);
            can_open_menu = false;
            auth.getCurrentUser().reload().addOnCompleteListener(task -> {
                can_open_menu = true;
                btn_singleplayer.setEnabled(true);
                btn_multiplayer.setEnabled(true);
                if (auth.getCurrentUser() != null) {
                    mainRef = (Firebase.userRef = Firebase.dataRef.child("Users").child(getUsername()));
                    if (first_open) {
                        new HelloDialog(MainActivity.this, getUsername()).show();
                        first_open = false;
                    }
                    tv_user_state.setText(String.format("%s: %s", getString(R.string.connected), getUsername()));
                    mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int localTime = getUsername().equals(auth.getCurrentUser().getDisplayName())? Stats.readStat(Stats.Readables.Time) : 0, serverTime;
                            if (dataSnapshot.hasChild("Time")) {
                                if (localTime > (serverTime = Objects.requireNonNull(dataSnapshot.child("Time").getValue(Integer.class)))) {
                                    mainRef.child("Time").setValue(localTime);
                                } else {
                                    Stats.setTime(serverTime);
                                }
                            } else {
                                mainRef.child("Time").setValue(localTime);
                            }
                            int localX = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Xwins) : 0, serverX;
                            if (dataSnapshot.hasChild("Xwins")) {
                                if (localX > (serverX = Objects.requireNonNull(dataSnapshot.child("Xwins").getValue(Integer.class)))) {
                                    mainRef.child("Xwins").setValue(localX);
                                } else {
                                    Stats.setXwins(serverX);
                                }
                            } else {
                                mainRef.child("Xwins").setValue(localX);
                            }
                            int localO = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Owins) : 0, serverO;
                            if (dataSnapshot.hasChild("Owins")) {
                                if (localO > (serverO = Objects.requireNonNull(dataSnapshot.child("Owins").getValue(Integer.class)))) {
                                    mainRef.child("Owins").setValue(localO);
                                } else {
                                    Stats.setOwins(serverO);
                                }
                            } else {
                                    mainRef.child("Owins").setValue(localO);
                                }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                } else {
                    btn_user_action.setText(R.string.login);
                    tv_user_state.setText(R.string.not_connected);
                }
            });
        } else {
            btn_user_action.setText(R.string.login);
            tv_user_state.setText(R.string.not_connected);
        }
        stats = false;
    }

    @Override
    protected void onPause() {
        if (!stats) {
            final long time = Utils.getTime();
            Stats.addTime(time);
            if (auth.getCurrentUser() != null && getUsername() != null) {
                try {
                    mainRef.child("Time").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mainRef.child("Time").setValue(Objects.requireNonNull(dataSnapshot.getValue(Integer.class)) + time);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                } catch (Exception ignored) {}
            }
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (can_open_menu) {
            switch (item.getItemId()) {
                case R.id.menu_statistics: {
                    final long time = Utils.getTime();
                    Stats.addTime(time);
                    stats = true;
                    new InternetTask(MainActivity.this, time).execute();
                    break;
                }

                case R.id.menu_settings: {
                    startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), SETTINGS_ACTIVITY);
                    break;
                }

                default: {
                    return super.onOptionsItemSelected(item);
                }
            }
        }
        return true;
    }

    @Override
    public void recreate() {
        finish();
        overridePendingTransition(0, 0);
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_AUTH) {
            if (resultCode == RESULT_OK) {  //User connected
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                setUsername(Objects.requireNonNull(user).getDisplayName());
                new HelloDialog(MainActivity.this, getUsername()).show();
                btn_user_action.setText(R.string.logout);
                tv_user_state.setText(String.format("%s: %s", getString(R.string.connected), getUsername()));
                (mainRef = Firebase.userRef = Firebase.dataRef.child("Users").child(getUsername())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int localTime = getUsername().equals(Objects.requireNonNull(auth.getCurrentUser()).getDisplayName()) ? Stats.readStat(Stats.Readables.Time) : 0, serverTime;
                        if (dataSnapshot.hasChild("Time")) {
                            if (localTime > (serverTime = Objects.requireNonNull(dataSnapshot.child("Time").getValue(Integer.class)))) {
                                mainRef.child("Time").setValue(localTime);
                            } else {
                                Stats.setTime(serverTime);
                            }
                        } else {
                            mainRef.child("Time").setValue(localTime);
                        }
                        int localX = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Xwins) : 0, serverX;
                        if (dataSnapshot.hasChild("Xwins")) {
                            if (localX > (serverX = Objects.requireNonNull(dataSnapshot.child("Xwins").getValue(Integer.class)))) {
                                mainRef.child("Xwins").setValue(localX);
                            } else {
                                Stats.setXwins(serverX);
                            }
                        } else {
                            mainRef.child("Xwins").setValue(localX);
                        }
                        int localO = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Owins) : 0, serverO;
                        if (dataSnapshot.hasChild("Owins")) {
                            if (localO > (serverO = Objects.requireNonNull(dataSnapshot.child("Owins").getValue(Integer.class)))) {
                                mainRef.child("Owins").setValue(localO);
                            } else {
                                Stats.setOwins(serverO);
                            }
                        } else {
                            mainRef.child("Owins").setValue(localO);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        } else if (requestCode == SETTINGS_ACTIVITY) {
            recreate();
        }
    }

    /**
     * This function returns the connected user's name
     *
     * @return the connected user's name
     */
    String getUsername() {
        return sharedPreferences.getString("name", null);
    }

    /**
     * This function sets the connected user's name
     *
     * @param name the connected user's name
     */
    void setUsername(String name) {
        sharedPreferences.edit().putString("name", name).apply();
    }

    /**
     * The AsyncTask to check if the user is connected to the internet
     */
    private static class InternetTask extends AsyncTask<Void, Void, Boolean> {
        /**
         * The weak reference to main activity object
         */
        private WeakReference<MainActivity> activityReference;
        private long time = 0;

        InternetTask(MainActivity activityReference, long time) {
            this.activityReference = new WeakReference<>(activityReference);
            this.time = time;
        }

        InternetTask(MainActivity activityReference) {
            this.activityReference = new WeakReference<>(activityReference);
        }

            @Override
            protected Boolean doInBackground(Void... voids) {
                try {
                    return InetAddress.getByName("www.google.com").isReachable(2000);
                } catch (Exception ignored) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    if (time != 0) {
                        if (activityReference.get().auth.getCurrentUser() != null) {
                            activityReference.get().mainRef.child("Time").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    activityReference.get().mainRef.child("Time").setValue(Objects.requireNonNull(dataSnapshot.getValue(Integer.class)) + time)
                                            .addOnCompleteListener(task -> activityReference.get().startActivity(new Intent(activityReference.get(), StatisticsActivity.class)));
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {}
                            });
                        } else {
                            activityReference.get().startActivity(new Intent(activityReference.get(), StatisticsActivity.class));
                        }
                    } else {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            new AlertDialog.Builder(activityReference.get())
                                    .setNegativeButton(R.string.host_game, (dialogInterface, i) -> activityReference.get()
                                            .startActivity(new Intent(activityReference.get(), OptionsActivity.class)
                                                    .putExtra("Mode", Utils.Mode.Multiplayer)))
                                    .setPositiveButton(R.string.join_game, (dialogInterface, i) -> activityReference.get()
                                            .startActivity(new Intent(activityReference.get(), JoinMultiplayerActivity.class)))
                                    .setTitle(R.string.multiplayer_settings)
                                    .setMessage(R.string.multiplayer_dialog)
                                    .show();
                        } else {
                            Toast.makeText(activityReference.get(), R.string.unauthed_user, Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (time != 0) {
                        activityReference.get().startActivity(new Intent(activityReference.get(), StatisticsActivity.class));
                    } else {
                        Toast.makeText(activityReference.get(), R.string.not_available_offline, Toast.LENGTH_LONG).show();
                    }
                }
                super.onPostExecute(aBoolean);
            }
    }
}
