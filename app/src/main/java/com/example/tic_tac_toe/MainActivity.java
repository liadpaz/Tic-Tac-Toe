package com.example.tic_tac_toe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

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

        btn_singleplayer.setOnClickListener(this);
        btn_mutltiplayer.setOnClickListener(this);
        btn_exit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == this.btn_singleplayer)
        {
            Intent settingsAct = new Intent(getApplicationContext(), SettingsActivity.class);
            settingsAct.putExtra("Mode", 0);
            startActivity(settingsAct);
        }
        else if (view == this.btn_mutltiplayer)
        {
            AlertDialog.Builder multiplayer = new AlertDialog.Builder(this);
            multiplayer.setNegativeButton("Host a game", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent settingsAct = new Intent(getApplicationContext(), SettingsActivity.class);
                    settingsAct.putExtra("Mode", 1);
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
        else //if (view ==  this.btn_exit)
        {
            this.finish();
            System.exit(0);
        }

    }
}
