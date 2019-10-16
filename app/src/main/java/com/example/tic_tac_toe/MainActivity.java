package com.example.tic_tac_toe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import static com.example.tic_tac_toe.Utils.Mode.Multiplayer;
import static com.example.tic_tac_toe.Utils.Mode.Singleplayer;

public class MainActivity extends AppCompatActivity {

    Button btn_singleplayer;
    Button btn_mutltiplayer;
    Button btn_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_singleplayer = findViewById(R.id.btn_singleplayer);
        btn_mutltiplayer = findViewById(R.id.btn_multiplayer);
        btn_exit = findViewById(R.id.btn_exit);

        btn_singleplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent settingsAct = new Intent(getApplicationContext(), SettingsActivity.class);
                settingsAct.putExtra("Mode", Singleplayer);
                startActivity(settingsAct);
            }
        });
        btn_mutltiplayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder multiplayer = new AlertDialog.Builder(MainActivity.this);
                multiplayer.setNegativeButton("Host a game", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent settingsAct = new Intent(getApplicationContext(), SettingsActivity.class);
                        settingsAct.putExtra("Mode", Multiplayer);
                        startActivity(settingsAct);
                    }
                }).setPositiveButton("Join a game", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(MainActivity.this, JoinMultiplayer.class));
                    }
                }).setTitle("Multiplayer Options").setMessage("Choose if you'd like to join a game / host a game").create();
                multiplayer.show();
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
}
