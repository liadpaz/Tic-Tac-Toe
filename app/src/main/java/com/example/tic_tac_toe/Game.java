package com.example.tic_tac_toe;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.net.ServerSocket;

public class Game extends AppCompatActivity implements View.OnClickListener {

    Player[] players = new Player[2];
    int maxgames;
    int timer;
    Utils.Mode mode;

    ImageView iv_board;
    Cell[][] cells = new Cell[3][3];

    Button btn_resign;
    Button btn_restart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        maxgames = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 0);
        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");

        iv_board = findViewById(R.id.iv_board);
        cells[0][0] = new Cell(new ImageView[]{findViewById(R.id.iv_Xtl), findViewById(R.id.iv_Otl)});
        cells[0][1] = new Cell(new ImageView[]{findViewById(R.id.iv_Xtm), findViewById(R.id.iv_Otm)});
        cells[0][2] = new Cell(new ImageView[]{findViewById(R.id.iv_Xtr), findViewById(R.id.iv_Otr)});
        cells[1][0] = new Cell(new ImageView[]{findViewById(R.id.iv_Xml), findViewById(R.id.iv_Oml)});
        cells[1][1] = new Cell(new ImageView[]{findViewById(R.id.iv_Xmm), findViewById(R.id.iv_Omm)});
        cells[1][2] = new Cell(new ImageView[]{findViewById(R.id.iv_Xmr), findViewById(R.id.iv_Omr)});
        cells[2][0] = new Cell(new ImageView[]{findViewById(R.id.iv_Xbl), findViewById(R.id.iv_Obl)});
        cells[2][1] = new Cell(new ImageView[]{findViewById(R.id.iv_Xbm), findViewById(R.id.iv_Obm)});
        cells[2][2] = new Cell(new ImageView[]{findViewById(R.id.iv_Xbr), findViewById(R.id.iv_Obr)});
        btn_resign = findViewById(R.id.btn_resign);

        btn_resign.setOnClickListener(this);

        int x = getApplicationContext().getResources().getDisplayMetrics().widthPixels;
        int y = getApplicationContext().getResources().getDisplayMetrics().heightPixels;

        iv_board.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j].setSize(getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3);
                cells[i][j].setLocation(j * (getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3), i * (getApplicationContext().getResources().getDisplayMetrics().widthPixels / 3));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mode == Utils.Mode.Singleplayer) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Which player you wish to be? (O Starts the game)")
                    .setTitle("Choose player")
                    .setPositiveButton("X", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.CPU, Cell.Type.O);
                            players[1] = new Player(Player.Type.Human, Cell.Type.X);
                        }
                    })
                    .setNegativeButton("O", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.Human, Cell.Type.O);
                            players[1] = new Player(Player.Type.CPU, Cell.Type.X);
                        }
                    });
            dialog.show();
        }
    }

//    void resetGame(boolean hardReset)
//    {
//
//    }

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

    public void setClickable(Cell[][] cells, Cell.Type type) {
        for (Cell[] temp : cells) {
            for (Cell cell : temp) {
                cell.setClickable(type);
            }
        }
    }
}