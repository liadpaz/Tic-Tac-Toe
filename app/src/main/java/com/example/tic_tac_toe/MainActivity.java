package com.example.tic_tac_toe;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static com.example.tic_tac_toe.Utils.Mode;

public class MainActivity extends AppCompatActivity {

    Button btn_singleplayer;
    Button btn_mutltiplayer;
    Button btn_stats;
    Button btn_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setTime();
        Stats.setFile(getFilesDir());

        btn_singleplayer = findViewById(R.id.btn_singleplayer);
        btn_mutltiplayer = findViewById(R.id.btn_multiplayer);
        btn_stats = findViewById(R.id.btn_stats);
        btn_exit = findViewById(R.id.btn_exit);

        btn_singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder singleplayer = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getString(R.string.Play_Local))
                        .setMessage(getString(R.string.Play_Local_Message))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.Computer), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)
                                        .putExtra("Mode", Mode.Computer));
                            }
                        })
                        .setNegativeButton(getString(R.string.Two_Players), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)
                                        .putExtra("Mode", Mode.TwoPlayer));
                            }
                        });
                singleplayer.show();
            }
        });
        btn_mutltiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder multiplayer = new AlertDialog.Builder(MainActivity.this)
                        .setNegativeButton("Host a game", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class)
                                        .putExtra("Mode", Mode.Multiplayer));
                            }
                        })
                        .setPositiveButton("Join a game", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                startActivity(new Intent(MainActivity.this, JoinMultiplayer.class));
                            }
                        })
                        .setTitle("Multiplayer Options")
                        .setMessage("Choose if you'd like to join a game / host a game");
                multiplayer.show();
            }
        });
        btn_stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Statistics.class));
            }
        });
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }

    @Override
    protected void onPause() {
        Stats.addTime(Utils.getTime());

        super.onPause();
    }
}
