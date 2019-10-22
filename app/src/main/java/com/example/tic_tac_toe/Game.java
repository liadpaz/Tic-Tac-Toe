package com.example.tic_tac_toe;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.tic_tac_toe.Cell.Type;

public class Game extends AppCompatActivity {

    ConstraintLayout layout_game;

    Type turn;
    Type this_type;
    int maxgames;
    int timer;
    Utils.Mode mode;
    Type startingType;
    CountDownTimer counter;

    DisplayMetrics screen;
    int topScreen;

    ImageView iv_board;
    Player[] players = new Player[2];
    Cell[][] cells = new Cell[3][3];
    Rect[][] over_cells = new Rect[3][3];

    Button btn_resign;
    Button btn_reset;

    TextView tv_time_text;
    TextView tv_timer;
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

        layout_game = findViewById(R.id.activity_game);

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
        tv_time_text = findViewById(R.id.tv_time_text);
        tv_timer = findViewById(R.id.tv_timer);

        tv_playerXwins.setText("0");
        tv_playerOwins.setText("0");
        tv_maxgames.setText(String.format("%s %s %s", getString(R.string.First_To), String.valueOf(maxgames), getString(R.string.Wins_wins)));

        if (timer != 0) {
            tv_time_text.setVisibility(View.VISIBLE);
            tv_timer.setVisibility(View.VISIBLE);
            tv_timer.setText(String.format("%s %s", String.valueOf(timer), getString(R.string.Seconds)));
            counter = new CountDownTimer(timer * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tv_timer.setText(String.format("%s %s", String.valueOf((int) millisUntilFinished / 1000 + 1), getString(R.string.Seconds)));
                }

                @Override
                public void onFinish() {
                    putCPU(turn == Type.O ? 0 : 1);
                    if (winner() == null) {
                        turn = turn == Type.O ? Type.X : Type.O;
                        if (allVisible())
                            tieAlert().show();
                        else if (vs_computer) {
                            putCPU();
                            if (winner() == null) {
                                turn = turn == Type.O ? Type.X : Type.O;
                                if (allVisible())
                                    tieAlert().show();
                                else
                                    this.start();
                            }
                        } else if (vs_on_this_device) {
                            this.start();
                        }
                    }
                }
            }.start();
        }

        btn_resign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder resignDialog = new AlertDialog.Builder(Game.this);
                resignDialog
                        .setTitle(getString(R.string.Resign))
                        .setMessage(getString(R.string.Resign_Message))
                        .setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.No), null)
                        .setCancelable(true);
                resignDialog.show();
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder restartDialog = new AlertDialog.Builder(Game.this);
                restartDialog
                        .setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                tv_playerOwins.setText("0");
                                tv_playerXwins.setText("0");
                                resetGame(true);
                            }
                        })
                        .setNegativeButton(getString(R.string.No), null)
                        .setCancelable(true)
                        .setTitle(getString(R.string.Restart))
                        .setMessage(getString(R.string.Restart_question));
                restartDialog.show();
            }
        });

        screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
        topScreen = screen.densityDpi / 2;
        int x = screen.widthPixels;

        iv_board.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                over_cells[i][j] = new Rect(j * (x / 3), i * (x / 3) + topScreen, (j + 1) * (x / 3), (i + 1) * (x / 3) + topScreen);
                cells[i][j].setSize(x / 3);
                cells[i][j].setLocation(j * (x / 3), i * (x / 3) + topScreen / 100);
            }
        }

        if (vs_computer) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.Player_Chooser))
                    .setTitle(getString(R.string.Choose_Player))
                    .setPositiveButton("X", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.CPU, Cell.Type.O);
                            players[1] = new Player(Player.Type.Human, Cell.Type.X);
                            tv_playerO.setText(getString(R.string.Computer));
                            tv_playerX.setText(getString(R.string.You));
                            tv_playerX.setVisibility(View.VISIBLE);
                            tv_playerO.setVisibility(View.VISIBLE);
                            tv_playerXwins.setVisibility(View.VISIBLE);
                            tv_playerOwins.setVisibility(View.VISIBLE);
                            if (startingType == Cell.Type.O) {
                                putCPU();
                                turn = Type.X;
                            }
                            if (timer != 0)
                                counter.start();
                        }
                    })
                    .setNegativeButton("O", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.Human, Cell.Type.O);
                            players[1] = new Player(Player.Type.CPU, Cell.Type.X);
                            tv_playerO.setText(getString(R.string.You));
                            tv_playerX.setText(getString(R.string.Computer));
                            tv_playerX.setVisibility(View.VISIBLE);
                            tv_playerO.setVisibility(View.VISIBLE);
                            tv_playerXwins.setVisibility(View.VISIBLE);
                            tv_playerOwins.setVisibility(View.VISIBLE);
                            if (startingType == Cell.Type.X) {
                                putCPU();
                                turn = Type.O;
                            }
                            if (timer != 0)
                                if (timer != 0) counter.start();

                        }
                    });
            dialog.show();
        } else {
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
     * This function gets triggered whenever the user touches the pScreen, and
     * decides whether to count as a valid XO press, if so, it makes the X / O
     * visible and checks for winner / tie.
     *
     * @param event touch location and info about the touch
    */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int touchX = (int) event.getRawX();
        final int touchY = (int) event.getRawY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (over_cells[i][j].contains(touchX, touchY) && !cells[i][j].isVisible() && (vs_computer || vs_on_this_device || (vs_multiplayer && turn == this_type))) {
                        if (play(i, j)) {
                            i = 3;
                            break;
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
                .setTitle(getString(R.string.Congratulations))
                .setMessage(String.format("%s %s %s", getString(R.string.Player), player_won, getString(R.string.Won)))
                .setPositiveButton(getString(R.string.Continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                        if (timer != 0 && notCPUturn())
                            counter.start();
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
                .setTitle(getString(R.string.Tie))
                .setMessage(getString(R.string.Its_a_tie))
                .setPositiveButton(getString(R.string.Continue), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetGame(false);
                        if (timer != 0 && notCPUturn())
                            counter.start();
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
                .setTitle(getString(R.string.Winner))
                .setMessage(String.format("%s %s %s", getString(R.string.Player),player_won, getString(R.string.Won_The_Game)))
                .setPositiveButton(getString(R.string.Continue), new DialogInterface.OnClickListener() {
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
            if (timer != 0) counter.cancel();
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
            else {
                if (vs_on_this_device || notCPUturn() || (vs_multiplayer && winner == this_type)) {
                    if (winner == Type.X)
                        Stats.addXwins();
                    else if (winner == Type.O)
                        Stats.addOwins();
                }
                absoluteWinnerAlert(winner.toString()).show();
            }
            return winner;
        }
        return null;
    }

    /**
     * This function resets the game with or without players score
     *
     * @param hardReset true if to reset also the players score
     */
    private void resetGame(boolean hardReset) {
        if (hardReset) {
            players[0] = new Player(players[0].playerType, Cell.Type.O);
            players[1] = new Player(players[1].playerType, Cell.Type.X);
        }
        hideAll();
        turn = startingType;
        if ((players[0].playerType == Player.Type.CPU && turn == Type.O) || (players[1].playerType == Player.Type.CPU && turn == Type.X)) {
            putCPU();
            turn = turn == Type.O ? Type.X : Type.O;
        }
    }

    /**
     * This function plays a CPU turn
     */
    private void putCPU() {
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
    }

    /**
     * This function plays a player who runs out of time turn
     *
     * @param playerIndex player index from {@code players} to play
     */
    private void putCPU(int playerIndex) {
        int[] rand = players[playerIndex].getRandom();
        while (!cells[rand[0]][rand[1]].setType(turn)) {
            rand = players[playerIndex].getRandom();
        }
    }

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

    /**
     * This function checks whether it's a CPU turn
     *
     * @return true if it's a CPU turn, otherwise false
     */
    private boolean notCPUturn() {
        return (((turn != Type.O || players[0].playerType != Player.Type.CPU) && (turn != Type.X || players[1].playerType != Player.Type.CPU)) || !vs_computer);
    }

    /**
     * This function plays a turn, if needed a CPU turn afterwards it does this
     *
     * @param i the y index of the cell
     * @param j the x index of the cell
     *
     * @return  true if no more turns allowed, otherwise false
     */
    private boolean play(int i, int j) {
        if (timer != 0) counter.cancel();
        cells[i][j].setType(turn);
        if (winner() != null) { //Winner is found
            if (timer != 0) counter.cancel();
            return true;
        }                       //No winner is found
        turn = turn == Cell.Type.X ? Cell.Type.O : Cell.Type.X; //Flip the turn
        if (allVisible()) {     //No winner and the board is full (tie)
            if (timer != 0) counter.cancel();
            tieAlert().show();
            return true;
        }
        if (vs_computer) {      //One of the players is CPU
            putCPU();
            if (winner() == null && allVisible()) { //If no winner and no vacant place
                if (timer != 0) counter.cancel();
                tieAlert().show();
                return true;
            }
            turn = turn == Cell.Type.X ? Cell.Type.O : Cell.Type.X;
        }
        if (timer != 0) counter.start();
        return false;
    }
}