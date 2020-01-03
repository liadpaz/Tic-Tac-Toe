package com.liadpaz.tic_tac_toe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

    private static final int USER_AUTH = 1;

    FirebaseAuth auth;
    DatabaseReference mainRef;

    boolean stats;

    TextView tv_title;

    Button btn_singleplayer;
    Button btn_multiplayer;
    CountDownTimer count;

    int devCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));

        new Stats(getSharedPreferences("tic-tac-toe-stats", 0));

        tv_title = findViewById(R.id.tv_title);
        btn_singleplayer = findViewById(R.id.btn_singleplayer);
        btn_multiplayer = findViewById(R.id.btn_multiplayer);

        count = new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                devCounter = 0;
            }
        };

        tv_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count.cancel();
                devCounter++;
                if (devCounter != 5) {
                    Toast.makeText(MainActivity.this, String.format("%s %s %s", getString(R.string.DevFirst), String.valueOf(5 - devCounter), getString(R.string.DevSecond)), Toast.LENGTH_SHORT).show();
                    count.start();
                } else {
                    devCounter = 0;
                    if (auth.getCurrentUser() != null) {
                        if (Objects.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail(), "liadpazhamud@gmail.com")) {
                            startActivity(new Intent(MainActivity.this, DeveloperActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, R.string.NotAuthorized, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, R.string.NotAuthorized, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        btn_singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        btn_multiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new InternetTask(MainActivity.this).execute();
            }
        });

        mainRef = Firebase.dataRef;
        auth = FirebaseAuth.getInstance();

        // If the user is authenticated
        if (auth.getCurrentUser() != null) {
            new HelloDialog(MainActivity.this, Objects.requireNonNull(auth.getCurrentUser().getDisplayName())).show();
            (mainRef = mainRef.child("Users").child(auth.getCurrentUser().getDisplayName())).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int localTime = Stats.readFile(Stats.Readables.Time), serverTime;
                    int localX = Stats.readFile(Stats.Readables.Xwins), serverX;
                    int localO = Stats.readFile(Stats.Readables.Owins), serverO;
                    if (localTime > (serverTime = Objects.requireNonNull(dataSnapshot.child("Time").getValue(Integer.class)))) {
                        mainRef.child("Time").setValue(localTime);
                    } else {
                        Stats.setTime(serverTime);
                    }
                    if (localX > (serverX = Objects.requireNonNull(dataSnapshot.child("Xwins").getValue(Integer.class)))) {
                        mainRef.child("Xwins").setValue(localX);
                    } else {
                        Stats.setXwins(serverX);
                    }
                    if (localO > (serverO = Objects.requireNonNull(dataSnapshot.child("Owins").getValue(Integer.class)))) {
                        mainRef.child("Time").setValue(localO);
                    } else {
                        Stats.setOwins(serverO);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        Utils.setTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stats = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_privacy).setChecked(Stats.readPrivacy());
        if (auth.getCurrentUser() != null) {
            menu.findItem(R.id.menu_login).setVisible(false);
        } else {
            menu.findItem(R.id.menu_logout).setVisible(false);
            menu.findItem(R.id.menu_delete).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (auth.getCurrentUser() != null) {
            menu.findItem(R.id.menu_login).setVisible(false);
            menu.findItem(R.id.menu_logout).setVisible(true);
            menu.findItem(R.id.menu_delete).setVisible(true);
        } else {
            menu.findItem(R.id.menu_login).setVisible(true);
            menu.findItem(R.id.menu_logout).setVisible(false);
            menu.findItem(R.id.menu_delete).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_statistics:
                final long time = Utils.getTime();
                Stats.addTime(time);
                if (auth.getCurrentUser() != null) {
                    stats = true;
                    mainRef.child("Time").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mainRef.child("Time").setValue(Objects.requireNonNull(dataSnapshot.getValue(Integer.class)) + time).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    startActivity(new Intent(MainActivity.this, Statistics.class));
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                break;

            case R.id.menu_privacy:
                boolean privacy = Stats.flipPrivacy();
                item.setChecked(privacy);
                Toast.makeText(MainActivity.this, privacy ? R.string.PrivacyActivated : R.string.PrivacyDeactivated, Toast.LENGTH_LONG).show();
                break;

            case R.id.menu_logout:
                AuthUI.getInstance().signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, R.string.LogoutMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;

            case R.id.menu_login:
                List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
                startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(), USER_AUTH);
                break;

            case R.id.menu_delete:
                AuthUI.getInstance().delete(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, R.string.DeleteUserSuccess, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.DeleteUserFail, Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;

            case R.id.menu_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onPause() {
        if (!stats) {
            final long time = Utils.getTime();
            Stats.addTime(time);
            if (auth.getCurrentUser() != null) {
                mainRef.child("Time").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mainRef.child("Time").setValue(Objects.requireNonNull(dataSnapshot.getValue(Integer.class)) + time);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == USER_AUTH) {
            if (resultCode == RESULT_OK) {  //User connected
                String name = Objects.requireNonNull(auth.getCurrentUser()).getDisplayName();
                new HelloDialog(MainActivity.this, Objects.requireNonNull(name)).show();
                (mainRef = Firebase.dataRef.child("Users").child(name)).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int localTime = Stats.readFile(Stats.Readables.Time), serverTime;
                        int localX = Stats.readFile(Stats.Readables.Xwins), serverX;
                        int localO = Stats.readFile(Stats.Readables.Owins), serverO;
                        if (localTime > (serverTime = Objects.requireNonNull(dataSnapshot.child("Time").getValue(Integer.class)))) {
                            mainRef.child("Time").setValue(localTime);
                        } else {
                            Stats.setTime(serverTime);
                        }
                        if (localX > (serverX = Objects.requireNonNull(dataSnapshot.child("Xwins").getValue(Integer.class)))) {
                            mainRef.child("Xwins").setValue(localX);
                        } else {
                            Stats.setXwins(serverX);
                        }
                        if (localO > (serverO = Objects.requireNonNull(dataSnapshot.child("Owins").getValue(Integer.class)))) {
                            mainRef.child("Time").setValue(localO);
                        } else {
                            Stats.setOwins(serverO);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
    }

    private static class InternetTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<MainActivity> activityReference;

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
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        AlertDialog.Builder multiplayer = new AlertDialog.Builder(activityReference.get())
                                .setNegativeButton(R.string.HostGame, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        activityReference.get().startActivity(new Intent(activityReference.get(), SettingsActivity.class)
                                                .putExtra("Mode", Utils.Mode.Multiplayer));
                                    }
                                })
                                .setPositiveButton(R.string.JoinGame, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        activityReference.get().startActivity(new Intent(activityReference.get(), JoinMultiplayer.class));
                                    }
                                })
                                .setTitle(R.string.MultiplayerOptions)
                                .setMessage(R.string.MultiplayerDialog);
                        multiplayer.show();
                    } else {
                        Toast.makeText(activityReference.get(), R.string.UnauthedUser, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(activityReference.get(), R.string.NotAvailableOffline, Toast.LENGTH_LONG).show();
                }
                super.onPostExecute(aBoolean);
            }
    }
}
