package com.example.tic_tac_toe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Game extends AppCompatActivity implements View.OnClickListener {

    int player1Win = 0, player2Win = 0;
    int maxgames;
    int timer;
    int mode;

    Button btn_resign;
    Button btn_resrt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        maxgames = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 0);
        mode = getIntent().getIntExtra("Mode", 0);

        btn_resign = findViewById(R.id.btn_resign);

        btn_resign.setOnClickListener(this);
    }

    void resetGame(boolean hardReset)
    {

    }

    @Override
    public void onClick(View view) {
        if (view == this.btn_resign)
        {
            AlertDialog.Builder resignDialog = new AlertDialog.Builder(this);
            resignDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            }).setNegativeButton("No", null).setCancelable(true).setTitle("Resign").setMessage("Are you sure you want to resign the current game?").create();
            resignDialog.show();
        }
    }
}
