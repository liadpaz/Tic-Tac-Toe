package com.example.tic_tac_toe;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.tic_tac_toe.Cell.Type;

public class Game extends AppCompatActivity {

    ConstraintLayout activity_game;

    Player[] players = new Player[2];
    Type turn;
    Type this_type;
    int maxgames;
    int timer;
    Utils.Mode mode;
    Type startingType;

    ImageView iv_board;
    Cell[][] cells = new Cell[3][3];
    Rect[][] over_cells = new Rect[3][3];

    Button btn_resign;
    Button btn_reset;

    TextView tv_playerX;
    TextView tv_playerXwins;
    TextView tv_playerO;
    TextView tv_playerOwins;
    TextView tv_maxgames;

    boolean vs_multiplayer = false;
    boolean vs_computer = false;
    boolean vs_on_this_device = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        maxgames = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 0);
        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");
        startingType = (Cell.Type) getIntent().getSerializableExtra("Starting");

        turn = startingType;

        vs_multiplayer = mode == Utils.Mode.Multiplayer;
        vs_computer = mode == Utils.Mode.Computer;
        vs_on_this_device = mode == Utils.Mode.TwoPlayer;

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
        tv_playerX = findViewById(R.id.tv_playerX);
        tv_playerXwins = findViewById(R.id.tv_playerXwins);
        tv_playerO = findViewById(R.id.tv_playerO);
        tv_playerOwins = findViewById(R.id.tv_playerOwins);
        tv_maxgames = findViewById(R.id.tv_maxgames);

        tv_playerX.setVisibility(View.INVISIBLE);
        tv_playerO.setVisibility(View.INVISIBLE);
        tv_playerXwins.setVisibility(View.INVISIBLE);
        tv_playerOwins.setVisibility(View.INVISIBLE);
        tv_playerXwins.setText("0");
        tv_playerOwins.setText("0");
        tv_maxgames.setText("First to " + maxgames + " wins, wins!");

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
                        .setTitle("Resign")
                        .setMessage("Are you sure you want to resign the current game?");
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
                                tv_playerOwins.setText("0");
                                tv_playerXwins.setText("0");
                                resetGame(true);
                            }
                        })
                        .setNegativeButton("No", null)
                        .setCancelable(true)
                        .setTitle("Restart")
                        .setMessage("Are you sure you want to restart the current game?");
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

        if (vs_computer) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Which player you wish to be?")
                    .setTitle("Choose player")
                    .setPositiveButton("X", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.CPU, Cell.Type.O);
                            players[1] = new Player(Player.Type.Human, Cell.Type.X);
                            tv_playerO.setText(players[0].getStringType());
                            tv_playerX.setText(players[1].getStringType());
                            tv_playerX.setVisibility(View.VISIBLE);
                            tv_playerO.setVisibility(View.VISIBLE);
                            tv_playerXwins.setVisibility(View.VISIBLE);
                            tv_playerOwins.setVisibility(View.VISIBLE);
                            if (startingType == Cell.Type.O) {
                                putCPU();
                                turn = Type.X;
                            }
                        }
                    })
                    .setNegativeButton("O", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.Human, Cell.Type.O);
                            players[1] = new Player(Player.Type.CPU, Cell.Type.X);
                            tv_playerO.setText(players[0].getStringType());
                            tv_playerX.setText(players[1].getStringType());
                            tv_playerX.setVisibility(View.VISIBLE);
                            tv_playerO.setVisibility(View.VISIBLE);
                            tv_playerXwins.setVisibility(View.VISIBLE);
                            tv_playerOwins.setVisibility(View.VISIBLE);
                            if (startingType == Cell.Type.X) {
                                putCPU();
                                turn = Type.O;
                            }

                        }
                    });
            dialog.show();
        } else if (vs_on_this_device) {
            players[0] = new Player(Player.Type.Human, Type.O);
            players[1] = new Player(Player.Type.Human, Type.X);
            tv_playerO.setText("O");
            tv_playerX.setText("X");
            tv_playerX.setVisibility(View.VISIBLE);
            tv_playerO.setVisibility(View.VISIBLE);
            tv_playerXwins.setVisibility(View.VISIBLE);
            tv_playerOwins.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function gets triggered whenever the user touches the screen, and
     * decides whether to count as a valid XO press, if so it makes the X / O
     * visible and checks for winner / tie.
     *
     * @param event touch location and info about the touch
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Point screen = new Point();
        getWindowManager().getDefaultDisplay().getSize(screen);
        final int touchX = (int) event.getRawX();
        final int touchY = (int) (event.getRawY() - (screen.y - activity_game.getHeight()));
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (over_cells[i][j].contains(touchX, touchY) && !cells[i][j].isVisible() && (vs_computer || vs_on_this_device || (vs_multiplayer && turn == this_type))) {
                        cells[i][j].setType(turn);
                        if (winner() != null) { //Winner is found
                            i = 3;
                            break;
                        }   //No winner is found
                        turn = turn == Cell.Type.X ? Cell.Type.O : Cell.Type.X; //Flip the turn
                        if (vs_computer) {  //One of the players is CPU
                            if (!putCPU()) {    //If the CPU couldn't place a X / O
                                tieAlert().show();
                                i = 3;
                                break;
                            }   //If the CPU could place a type
                            if (winner() == null && allVisible()) { //If no winner and no vacant place
                                tieAlert().show();
                                i = 3;
                                break;
                            }
                            turn = turn == Cell.Type.X ? Cell.Type.O : Cell.Type.X;
                        } else if (vs_on_this_device) {
                            if (allVisible()) {
                                tieAlert().show();
                                i = 3;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * This function builds and returns a winner AlertDialog with the winner name in it
     *
     * @param player_won    player_won: a String contains the player name
     *
     * @return              winner AlertDialog
     */
    private AlertDialog.Builder winnerAlert(String player_won) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.Winner))
                .setMessage(getString(R.string.Player) + player_won + getString(R.string.Won))
                .setPositiveButton(getString(R.string.Continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                    }
                });
    }

    /**
     * This function builds and returns a tie AlertDialog
     *
     * @return tie AlertDialog
     */
    private AlertDialog.Builder tieAlert() {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Tie")
                .setMessage("It's a tie!")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                    }
                });
    }

    /**
     * This function checks if there is a winner
     *
     * <p>This function builds and returns an absolute winner AlertDialog with the winner name in it</p>
     *
     * @param player_won    a String contains the player name
     *
     * @return              absolute winner AlertDialog
     */
    private AlertDialog.Builder absoluteWinnerAlert(String player_won) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Winner")
                .setMessage("Player " + player_won + " Won The Game!")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        startActivity(new Intent(Game.this, MainActivity.class));
                    }
                });
    }

    /**
     * This function checks if there is a winner
     *
     * <p>This function checks if there is a winner and returns the type of the
     * winner if there is, otherwise returns null</p>
     *
     * @return type of the winner if there is one, otherwise null
     */
    private Type winner() {
        if (checkWinner()) {
            Type winner = turn;
            if (winner == Type.O) {
                players[0].won();
                tv_playerOwins.setText(players[0].getWins());
            } else {
                players[1].won();
                tv_playerXwins.setText(players[1].getWins());
            }
            if (!(Integer.parseInt(players[0].getWins()) == maxgames || Integer.parseInt(players[1].getWins()) == maxgames))
                winnerAlert(winner.toString()).show();
            else
                absoluteWinnerAlert(winner.toString()).show();
            return winner;
        }
        return null;
    }

    private void resetGame(boolean hardReset) {
        if (hardReset) {
            players[0] = new Player(players[0].playerType, Cell.Type.O);
            players[1] = new Player(players[1].playerType, Cell.Type.X);
        }
        hideAll();
        turn = startingType;
        if ((players[0].playerType == Player.Type.CPU && players[0].getXO() == turn) || (players[1].playerType == Player.Type.CPU && players[1].getXO() == turn)) {
            putCPU();
            turn = turn == Type.O ? Type.X : Type.O;
        }
    }

    private boolean putCPU() {
        if (allVisible()) return false;
        if (players[0].playerType == Player.Type.CPU) {
            int[] rand = players[0].getRandom();
            while (!cells[rand[0]][rand[1]].setType(turn)) {
                rand = players[0].getRandom();
            }
        } else {
            int[] rand = players[1].getRandom();
            while (!cells[rand[0]][rand[1]].setType(turn)) {
                rand = players[1].getRandom();
            }
        }
        return true;
    }

//    private boolean putCPU(int playerIndex) {
//        if (allVisible()) return false;
//        int[] rand = players[playerIndex].getRandom();
//        while (!cells[rand[0]][rand[1]].setType(turn)) {
//            rand = players[playerIndex].getRandom();
//        }
//        return true;
//    }

    /**
     * This function checks if all the X's / O's are visible
     *
     * @return true if all the X's / O's are visible, otherwise false
     */
    private boolean allVisible() {
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                if (!cell.isVisible()) return false;
            }
        }
        return true;
    }

    /**
     * This function hides all the X's and O's
     */
    private void hideAll() {
        for (Cell[] cellRow : cells)
            for (Cell cell : cellRow)
                cell.hide();
    }

    /**
     * This function checks if there is a winner
     *
     * @return true if there is a winner, otherwise false
     */
    private boolean checkWinner() {
        for (int i = 0; i < 3; i++) {
            if (checkColumn(i)) return true;
            if (checkRow(i)) return true;
        }
        return checkDiagonals();
    }

    /**
     * This function checks if there is a winner on the {@code column} row
     *
     * @param column    the row to check
     *
     * @return          true if there is a winner on the {@code column} row, otherwise false
     */
    private boolean checkRow(int column) {
        return cells[column][0].getType() == cells[column][1].getType() && cells[column][1].getType() == cells[column][2].getType() && cells[column][0].getType() != null;
    }

    /**
     * This function checks if there is a winner on the {@code row} column
     *
     * @param row   the column to check
     *
     * @return      true if there is a winner on the {@code row} column, otherwise false
     */
    private boolean checkColumn(int row) {
        return cells[0][row].getType() == cells[1][row].getType() && cells[1][row].getType() == cells[2][row].getType() && cells[0][row].getType() != null;
    }

    /**
     * This function checks if there is winner on one of the diagonals
     *
     * @return true if there is a winner on one the diagonals, otherwise false
     */
    private boolean checkDiagonals() {
        return ((cells[0][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[2][2].getType() && cells[0][0].getType() != null) ||
                (cells[2][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[0][2].getType() && cells[2][0].getType() != null));
    }
}