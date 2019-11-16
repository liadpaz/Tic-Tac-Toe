package com.liadpaz.tic_tac_toe;

import android.content.DialogInterface;
import android.content.Intent;
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

import static com.liadpaz.tic_tac_toe.Utils.Mode;

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
        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

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
            @Override
            public void onClick(View v) {
                if (Utils.isConnected()) {
                    AlertDialog.Builder multiplayer = new AlertDialog.Builder(MainActivity.this)
                            .setNegativeButton(getString(R.string.HostGame), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class)
                                            .putExtra("Mode", Mode.Multiplayer));
                                }
                            })
                            .setPositiveButton(getString(R.string.JoinGame), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    startActivity(new Intent(MainActivity.this, JoinMultiplayer.class));
                                }
                            })
                            .setTitle(getString(R.string.MultiplayerOptions))
                            .setMessage(R.string.MultiplayerDialog);
                    multiplayer.show();
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.NotAvailableOffline), Toast.LENGTH_LONG).show();
                }
            }
        });

        mainRef = Firebase.dataRef;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tictactoe_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_statistics:
                startActivity(new Intent(MainActivity.this, Statistics.class));
                return true;

            case R.id.menu_privacy:
                boolean privacy = Stats.flipPrivacy();
                Toast.makeText(MainActivity.this, privacy ? getString(R.string.PrivacyActivated) : getString(R.string.PrivacyDeactivated), Toast.LENGTH_LONG).show();
                return true;

            case R.id.menu_exit:
                System.exit(0);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {

        mainRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int localOwins = Stats.readFile(Stats.Readables.Owins);
                int localXwins = Stats.readFile(Stats.Readables.Xwins);
                int localTime  = Stats.readFile(Stats.Readables.Time);

                int globalOwins = dataSnapshot.child("Owins").getValue(Integer.class);
                int globalXwins = dataSnapshot.child("Xwins").getValue(Integer.class);
                int globalTime  = dataSnapshot.child("Time").getValue(Integer.class);

                Stats.addTime(Utils.getTime());

                mainRef.child("Owins").setValue(globalOwins - localOwins + Stats.readFile(Stats.Readables.Owins));
                mainRef.child("Xwins").setValue(globalXwins - localXwins + Stats.readFile(Stats.Readables.Xwins));
                mainRef.child("Time").setValue(globalTime - localTime + Stats.readFile(Stats.Readables.Time));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        super.onPause();
    }
}
