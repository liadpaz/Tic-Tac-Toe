package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    DatabaseReference mainRef;

    TextView tv_title;

    Button btn_singleplayer;
    Button btn_mutltiplayer;
    CountDownTimer count;

    int devCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_main));

        Utils.setTime();
        Stats.setFile(getFilesDir());

        tv_title = findViewById(R.id.tv_title);
        btn_singleplayer = findViewById(R.id.btn_singleplayer);
        btn_mutltiplayer = findViewById(R.id.btn_multiplayer);

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
                    startActivity(new Intent(MainActivity.this, DeveloperActivity.class));
                }
            }
        });

        btn_singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        btn_mutltiplayer.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, Boolean>() {
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
                            AlertDialog.Builder multiplayer = new AlertDialog.Builder(MainActivity.this)
                                    .setNegativeButton(R.string.HostGame, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startActivity(new Intent(MainActivity.this, SettingsActivity.class)
                                                    .putExtra("Mode", Utils.Mode.Multiplayer));
                                        }
                                    })
                                    .setPositiveButton(R.string.JoinGame, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            startActivity(new Intent(MainActivity.this, JoinMultiplayer.class));
                                        }
                                    })
                                    .setTitle(R.string.MultiplayerOptions)
                                    .setMessage(R.string.MultiplayerDialog);
                            multiplayer.show();
                        } else {
                            Toast.makeText(MainActivity.this, R.string.NotAvailableOffline, Toast.LENGTH_LONG).show();
                        }
                        super.onPostExecute(aBoolean);
                    }
                }.execute();
            }
        });

        mainRef = Firebase.dataRef;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.menu_privacy).setChecked(Stats.readPrivacy());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_statistics:
                startActivity(new Intent(MainActivity.this, Statistics.class));
                break;

            case R.id.menu_privacy:
                boolean privacy = Stats.flipPrivacy();
                item.setChecked(privacy);
                Toast.makeText(MainActivity.this, privacy ? R.string.PrivacyActivated : R.string.PrivacyDeactivated, Toast.LENGTH_LONG).show();
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
        final long time = Utils.getTime();
        Stats.addTime(time);
        mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int globalTime  = Objects.requireNonNull(dataSnapshot.child("Time").getValue(Integer.class));
                mainRef.child("Time").setValue(globalTime + time);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        super.onPause();
    }
}
