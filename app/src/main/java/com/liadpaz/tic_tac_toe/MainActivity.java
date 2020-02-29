package com.liadpaz.tic_tac_toe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.liadpaz.tic_tac_toe.databinding.ActivityMainBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final int USER_AUTH = 578;
    private static final int SETTINGS_ACTIVITY = 275;
    private static boolean first_open = true;
    private FirebaseAuth auth;
    private DatabaseReference mainRef;
    private boolean stats;

    private TextView tv_user_state;
    private Button btn_singleplayer;
    private Button btn_multiplayer;
    private Button btn_user_action;

    private CountDownTimer count;
    private SharedPreferences sharedPreferences;

    private int devCounter;
    private boolean can_open_menu = true;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarMain);
        getSupportActionBar().setTitle(null);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean mode = sharedPreferences.getBoolean(SettingsActivity.DARK_MODE_PREFERENCES, true);
        if (mode && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            recreate();
        } else if (!mode && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_NO) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        }

        new Stats(PreferenceManager.getDefaultSharedPreferences(MainActivity.this));
        auth = FirebaseAuth.getInstance();

        binding.tvTitle.setOnClickListener(v -> {
            count.cancel();
            devCounter++;
            if (devCounter != 5) {
                Toast.makeText(MainActivity.this, String.format("%s %s %s", MainActivity.this.getString(R.string.dev_first), String.valueOf(5 - devCounter), MainActivity.this.getString(R.string.dev_second)), Toast.LENGTH_SHORT).show();
                count.start();
            } else {
                devCounter = 0;
                if (auth.getCurrentUser() != null) {
                    if (Objects.equals(auth.getCurrentUser().getEmail(), "liadpazhamud@gmail.com")) {
                        startActivity(new Intent(MainActivity.this, DeveloperActivity.class));
                    } else {
                        Toast.makeText(MainActivity.this, R.string.not_authorized, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.not_authorized, Toast.LENGTH_LONG).show();
                }
            }
        });
        tv_user_state = binding.tvUserState;
        btn_singleplayer = binding.btnSingleplayer;
        btn_multiplayer = binding.btnMultiplayer;
        btn_user_action = binding.btnUserAction;

        count = new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                devCounter = 0;
            }
        };

        btn_singleplayer.setOnClickListener(v -> MainActivity.this.startActivity(new Intent(MainActivity.this, OptionsActivity.class)));
        btn_multiplayer.setOnClickListener(v -> {
            registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    //noinspection deprecation
                    NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    //noinspection deprecation
                    NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                    boolean isConnected = (wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected());
                    if (isConnected) {
                        if (auth.getCurrentUser() != null) {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setNegativeButton(R.string.host_game, (dialogInterface, i) -> startActivity(new Intent(MainActivity.this, OptionsActivity.class)
                                            .putExtra("Mode", Utils.Mode.Multiplayer)))
                                    .setPositiveButton(R.string.join_game, (dialogInterface, i) -> startActivity(new Intent(MainActivity.this, JoinMultiplayerActivity.class)))
                                    .setTitle(R.string.multiplayer_settings)
                                    .setMessage(R.string.multiplayer_dialog)
                                    .show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.unauthed_user, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.not_available_offline, Toast.LENGTH_LONG).show();
                    }
                    context.unregisterReceiver(this);
                }
            }, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            //new InternetTask(MainActivity.this).execute());
        });
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
            can_open_menu = false;
            btn_user_action.setText(R.string.logout);
            btn_singleplayer.setEnabled(false);
            btn_multiplayer.setEnabled(false);
            auth.getCurrentUser().reload().addOnCompleteListener(task -> {
                setUsername(auth.getCurrentUser().getDisplayName());
                btn_singleplayer.setEnabled(true);
                btn_multiplayer.setEnabled(true);
                if (auth.getCurrentUser() != null) {
                    mainRef = (Firebase.userRef = Firebase.dataRef.child("Users").child(auth.getCurrentUser().getUid()));
                    if (first_open) {
                        new HelloDialog(MainActivity.this, getUsername()).show();
                        first_open = false;
                    }
                    can_open_menu = true;
                    tv_user_state.setText(String.format("%s: %s", getString(R.string.connected), getUsername()));
                    mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            int localTime = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Time) : 0, serverTime;
                            if (dataSnapshot.hasChild("Time")) {
                                if (localTime > (serverTime = dataSnapshot.child("Time").getValue(Integer.class))) {
                                    mainRef.child("Time").setValue(localTime);
                                } else {
                                    Stats.setTime(serverTime);
                                }
                            } else {
                                mainRef.child("Time").setValue(localTime);
                            }
                            int localX = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Xwins) : 0, serverX;
                            if (dataSnapshot.hasChild("Xwins")) {
                                if (localX > (serverX = dataSnapshot.child("Xwins").getValue(Integer.class))) {
                                    mainRef.child("Xwins").setValue(localX);
                                } else {
                                    Stats.setXwins(serverX);
                                }
                            } else {
                                mainRef.child("Xwins").setValue(localX);
                            }
                            int localO = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Owins) : 0, serverO;
                            if (dataSnapshot.hasChild("Owins")) {
                                if (localO > (serverO = dataSnapshot.child("Owins").getValue(Integer.class))) {
                                    mainRef.child("Owins").setValue(localO);
                                } else {
                                    Stats.setOwins(serverO);
                                }
                            } else {
                                mainRef.child("Owins").setValue(localO);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
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
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mainRef.child("Time").setValue(dataSnapshot.getValue(Integer.class) + time);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } catch (Exception ignored) {
                }
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

                    BroadcastReceiver checkInternet = new BroadcastReceiver() {
                        @SuppressWarnings("ConstantConditions")
                        @Override
                        public void onReceive(@NotNull Context context, Intent intent) {
                            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            //noinspection deprecation
                            NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            //noinspection deprecation
                            NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                            boolean isConnected = (wifi != null && wifi.isConnected()) || (mobile != null && mobile.isConnected());
                            if (isConnected) {
                                if (auth.getCurrentUser() != null) {
                                    mainRef.child("Time").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @SuppressWarnings("ConstantConditions")
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            mainRef.child("Time").setValue(dataSnapshot.getValue(Integer.class) + time).addOnCompleteListener(task -> startActivity(new Intent(MainActivity.this, StatisticsActivity.class)));
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                } else {
                                    startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                                }
                            } else {
                                startActivity(new Intent(MainActivity.this, StatisticsActivity.class));
                            }
                            context.unregisterReceiver(this);
                        }
                    };
                    registerReceiver(checkInternet, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
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

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_AUTH) {
            if (resultCode == RESULT_OK) {  //User connected
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                setUsername(user.getDisplayName());
                new HelloDialog(MainActivity.this, getUsername()).show();
                btn_user_action.setText(R.string.logout);
                tv_user_state.setText(String.format("%s: %s", getString(R.string.connected), getUsername()));
                (mainRef = Firebase.userRef = Firebase.dataRef.child("Users").child(user.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int localTime = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Time) : 0, serverTime;
                        if (dataSnapshot.hasChild("Time")) {
                            if (localTime > (serverTime = dataSnapshot.child("Time").getValue(Integer.class))) {
                                mainRef.child("Time").setValue(localTime);
                            } else {
                                Stats.setTime(serverTime);
                            }
                        } else {
                            mainRef.child("Time").setValue(localTime);
                        }
                        int localX = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Xwins) : 0, serverX;
                        if (dataSnapshot.hasChild("Xwins")) {
                            if (localX > (serverX = dataSnapshot.child("Xwins").getValue(Integer.class))) {
                                mainRef.child("Xwins").setValue(localX);
                            } else {
                                Stats.setXwins(serverX);
                            }
                        } else {
                            mainRef.child("Xwins").setValue(localX);
                        }
                        int localO = getUsername().equals(auth.getCurrentUser().getDisplayName()) ? Stats.readStat(Stats.Readables.Owins) : 0, serverO;
                        if (dataSnapshot.hasChild("Owins")) {
                            if (localO > (serverO = dataSnapshot.child("Owins").getValue(Integer.class))) {
                                mainRef.child("Owins").setValue(localO);
                            } else {
                                Stats.setOwins(serverO);
                            }
                        } else {
                            mainRef.child("Owins").setValue(localO);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
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
    private String getUsername() {
        return sharedPreferences.getString("name", null);
    }

    /**
     * This function sets the connected user's name
     *
     * @param name the connected user's name
     */
    private void setUsername(String name) {
        sharedPreferences.edit().putString("name", name).apply();
    }
}
