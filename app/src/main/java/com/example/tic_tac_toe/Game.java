package com.example.tic_tac_toe;

import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.jetbrains.annotations.Nullable;

public class Game extends AppCompatActivity {

    ConstraintLayout activity_game;

    Player[] players = new Player[2];
    Cell.Type turn = Cell.Type.O;
    Cell.Type this_type;
    int maxgames;
    int timer;
    Utils.Mode mode;

    ImageView iv_board;
    Cell[][] cells = new Cell[3][3];
    Rect[][] over_cells = new Rect[3][3];

    Button btn_resign;
    Button btn_reset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        maxgames = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 0);
        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");

        activity_game = findViewById(R.id.activity_game);

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
        btn_reset = findViewById(R.id.btn_reset);

        btn_resign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder resignDialog = new AlertDialog.Builder(Game.this);
                resignDialog
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                            }
                        })
                        .setNegativeButton("No", null)
                        .setCancelable(true)
                        .setTitle("Resign").setMessage("Are you sure you want to resign the current game?").create();
                resignDialog.show();
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder restartDialog = new AlertDialog.Builder(Game.this);
                restartDialog
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                resetGame(true);
                            }
                        })
                        .setNegativeButton("No", null)
                        .setCancelable(true)
                        .setTitle("Restart").setMessage("Are you sure you want to restart the current game?").create();
                restartDialog.show();
            }
        });


        int x = getApplicationContext().getResources().getDisplayMetrics().widthPixels;

        iv_board.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                over_cells[i][j] = new Rect(j * (x / 3), i * (x / 3), (j + 1) * (x / 3), (i + 1) * (x / 3));
                cells[i][j].setSize(x / 3);
                cells[i][j].setLocation(j * (x / 3), i * (x / 3));
            }
        }

        if (mode == Utils.Mode.Singleplayer) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Which player you wish to be? (O Starts the game)")
                    .setTitle("Choose player")
                    .setPositiveButton("X", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            this_type = Cell.Type.X;
                            players[0] = new Player(Player.Type.CPU, Cell.Type.O);
                            players[1] = new Player(Player.Type.Human, Cell.Type.X);
                            putCPU();
                        }
                    })
                    .setNegativeButton("O", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            this_type = Cell.Type.O;
                            players[0] = new Player(Player.Type.Human, Cell.Type.O);
                            players[1] = new Player(Player.Type.CPU, Cell.Type.X);
                        }
                    });
            dialog.show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point screen = new Point();
        getWindowManager().getDefaultDisplay().getSize(screen);
        final float touchX = event.getRawX();
        final float touchY = event.getRawY() - (screen.y - activity_game.getHeight());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (over_cells[i][j].contains((int) touchX, (int) touchY) && turn == this_type && !cells[i][j].isVisible()) {
                        cells[i][j].setType(turn);
                        if (mode == Utils.Mode.Singleplayer) {
                            if (turn == Cell.Type.X) {
                                turn = Cell.Type.O;
                                if (winner() == null && !allVisible()) {
                                    putCPU();
                                } else {
                                    if (allVisible()) {
                                        tieAlert().show();
                                        resetGame(false);
                                    }
                                    i = 3;
                                    break;
                                }
                            }
                            else {
                                turn = Cell.Type.X;
                                if (winner() == null && !allVisible()) {
                                    putCPU();
                                } else {
                                    if (allVisible()) {
                                        tieAlert().show();
                                        resetGame(false);
                                    }
                                    i = 3;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private AlertDialog.Builder winnerAlert(String player_won) {
        return new AlertDialog.Builder(this)
                .setTitle("Winner")
                .setMessage("Player " + player_won + " Won!")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                    }
                });
    }

    private AlertDialog.Builder tieAlert() {
        return new AlertDialog.Builder(this)
                .setTitle("Tie")
                .setMessage("It's a tie!")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                    }
                });
    }

    @Nullable
    private Cell.Type winner() {
        if (checkWinner()) {
            Toast.makeText(getApplicationContext(), "WINNER", Toast.LENGTH_LONG).show();
            Cell.Type winner = turn == Cell.Type.O ? Cell.Type.X : Cell.Type.O;
            winnerAlert(winner.toString()).show();
            resetGame(false);
            if (winner == Cell.Type.O) players[0].won();
            else players[1].won();
            return winner;
        }
        return null;
    }

    private boolean checkWinner() {
        for (int i = 0; i < 3; i++) {
            if (checkColumn(i)) return true;
            if (checkRow(i)) return true;
        }
        return checkDiagonals();
    }

    private boolean checkRow(int column) {
        return cells[column][0].getType() == cells[column][1].getType() && cells[column][1].getType() == cells[column][2].getType() && cells[column][0].getType() != null;
    }

    private boolean checkColumn(int row) {
        return cells[0][row].getType() == cells[1][row].getType() && cells[1][row].getType() == cells[2][row].getType() && cells[0][row].getType() != null;
    }

    private boolean checkDiagonals() {
        return ((cells[0][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[2][2].getType() && cells[0][0].getType() != null) ||
                (cells[2][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[0][2].getType() && cells[2][0].getType() != null));
    }

    private void resetGame(boolean hardReset)
    {
        if (hardReset) {
            players[0] = new Player(players[0].playerType, Cell.Type.O);
            players[1] = new Player(players[1].playerType, Cell.Type.X);
        }
        hideAll();
        turn = Cell.Type.O;
        if (players[0].playerType == Player.Type.CPU)
            putCPU();
    }

    private boolean putCPU() {
        try {
            if (players[0].playerType == Player.Type.CPU) {
                int[] rand = players[0].getRandom();
                while (!cells[rand[0]][rand[1]].setType(turn) && !allVisibleExeption()) {
                    rand = players[0].getRandom();
                }
                turn = Cell.Type.X;
            } else {
                int[] rand = players[1].getRandom();
                while (!cells[rand[0]][rand[1]].setType(turn) && !allVisibleExeption()) {
                    rand = players[1].getRandom();
                }
                turn = Cell.Type.O;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean allVisibleExeption() throws Exception {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                if (!cell.isVisible()) return false;
            }
        }
        throw new Exception();
    }

    private boolean allVisible() {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                if (!cell.isVisible()) return false;
            }
        }
        return false;
    }

    private void hideAll() {
        for (Cell[] cellRow : cells)
            for (Cell cell : cellRow)
                cell.hide();
    }
}