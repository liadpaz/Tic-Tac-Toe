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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.tic_tac_toe.Cell.Type;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Game extends AppCompatActivity {

    DatabaseReference gameRef;

    ConstraintLayout layout_game;

    Type turn;
    Type thisType;
    int maxGames;
    int timer;
    Utils.Mode mode;
    Type startingType;
    CountDownTimer counter;

    DisplayMetrics screen;
    int topScreen;

    String thisName;
    String otherName;
    String Xname;
    String Oname;
    String lobbyNumber;
    String multiType;
    String lastHostMessage;
    String lastClientMessage;
    String lastMessage;
    String exitStatus;
    boolean canPlay;

    ImageView iv_board;
    Player[] players = new Player[2];
    volatile Cell[][] cells = new Cell[3][3];
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
    TextView tv_turn;

    boolean vs_multiplayer;
    boolean vs_computer;
    boolean vs_on_this_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        maxGames = getIntent().getIntExtra("Max", -1);
        timer = getIntent().getIntExtra("Timer", -1);
        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");
        startingType = (Cell.Type) getIntent().getSerializableExtra("Starting");
        lobbyNumber = getIntent().getStringExtra("LobbyNumber");
        multiType = getIntent().getStringExtra("Multiplayer");

        turn = startingType;

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
        tv_turn = findViewById(R.id.tv_turn);

        vs_multiplayer = mode == null;

        if (!vs_multiplayer) {
            vs_computer = mode == Utils.Mode.Computer;
            vs_on_this_device = mode == Utils.Mode.TwoPlayer;
            initialize();
        } else {

            canPlay = true;

            btn_reset.setVisibility(View.INVISIBLE);

            gameRef = Database.dataRef.child("Lobbies").child(lobbyNumber);

            gameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (Xname == null || Oname == null) {
                        if (dataSnapshot.child("hostType").getValue(String.class).equals("X")) {
                            thisType = multiType.equals("Host") ? Type.X : Type.O;
                            Xname = dataSnapshot.child("hostName").getValue(String.class);
                            Oname = dataSnapshot.child("clientName").getValue(String.class);

                            thisName = multiType.equals("Host") ? Xname : Oname;
                            otherName = multiType.equals("Host") ? Oname : Xname;
                        } else {
                            thisType = multiType.equals("Host") ? Type.O : Type.X;
                            Xname = dataSnapshot.child("clientName").getValue(String.class);
                            Oname = dataSnapshot.child("hostName").getValue(String.class);

                            thisName = multiType.equals("Host") ? Oname : Xname;
                            otherName = multiType.equals("Host") ? Xname : Oname;
                        }
                    }

                    if (timer == -1 || maxGames == -1 || thisName == null) {
                        timer = dataSnapshot.child("timer").getValue(Integer.class);
                        maxGames = dataSnapshot.child("max").getValue(Integer.class);

                        players[0] = new Player(Player.Type.Human);
                        players[1] = new Player(Player.Type.Human);
                        tv_playerO.setText(Oname);
                        tv_playerX.setText(Xname);
                        tv_playerX.setVisibility(View.VISIBLE);
                        tv_playerO.setVisibility(View.VISIBLE);
                        tv_playerXwins.setVisibility(View.VISIBLE);
                        tv_playerOwins.setVisibility(View.VISIBLE);
                        initialize();
                    }

                    if (startingType == null) {
                        startingType = dataSnapshot.child("startingType").getValue(String.class).equals("X") ? Type.X : Type.O;
                        turn = startingType;
                        tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.Its), thisType == startingType ? thisName : otherName, getString(R.string.Turn), startingType.toString()));
                    }

                    if (multiType.equals("Host")) {

                        String message = dataSnapshot.child("clientMessage").getValue(String.class);
                        if (lastClientMessage == null)
                            lastClientMessage = message;

                        if (message != null && !message.equals(lastClientMessage)) {
                            String[] messages = message.split(" ");

                            switch (messages[0]) {
                                case "turn":
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;

                                case "resign":
                                    if (thisType == Type.X)
                                        Stats.addXwins();
                                    else
                                        Stats.addOwins();
                                    absoluteWinnerAlert(false, thisName).show();
                                    break;

                                case "go":
                                    canPlay = true;
                                    lastMessage = "go";
                                    turn = startingType;
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.Its), thisType == startingType ? thisName : otherName, getString(R.string.Turn), startingType.toString()));
                                    break;
                            }
                        }

                        lastClientMessage = message;
                    } else {

                        String message = dataSnapshot.child("hostMessage").getValue(String.class);
                        if (lastHostMessage == null)
                            lastHostMessage = message;

                        if (message != null && !message.equals(lastHostMessage)) {
                            String[] messages = message.split(" ");

                            switch (messages[0]) {
                                case "turn":
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;

                                case "resign":
                                    if (thisType == Type.X)
                                        Stats.addXwins();
                                    else
                                        Stats.addOwins();
                                    absoluteWinnerAlert(false, thisName).show();
                                    break;

                                case "go":
                                    canPlay = true;
                                    lastMessage = "go";
                                    turn = startingType;
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.Its), thisType == turn ? thisName : otherName, getString(R.string.Turn), turn.toString()));
                                    break;
                            }
                        }

                        lastHostMessage = message;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        tv_playerXwins.setText("0");
        tv_playerOwins.setText("0");

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
                                if (vs_multiplayer)
                                    exitStatus = "resign";
                                finishAffinity();
                                startActivity(new Intent(Game.this, MainActivity.class));
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
        iv_board.setX(0);
        iv_board.setY(0);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                over_cells[i][j] = new Rect(j * (x / 3), i * (x / 3) + topScreen, (j + 1) * (x / 3), (i + 1) * (x / 3) + topScreen);
                cells[i][j].setSize(x / 3);
                cells[i][j].setLocation(j * (x / 3), i * (x / 3) + topScreen / 100);
            }
        }

        if (vs_computer) {

            tv_turn.setVisibility(View.INVISIBLE);

            AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.Player_Chooser))
                    .setTitle(getString(R.string.Choose_Player))
                    .setPositiveButton("X", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            players[0] = new Player(Player.Type.CPU);
                            players[1] = new Player(Player.Type.Human);
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
                            players[0] = new Player(Player.Type.Human);
                            players[1] = new Player(Player.Type.CPU);
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
                                counter.start();

                        }
                    });
            dialog.show();

        } else if (vs_on_this_device) {

            tv_turn.setText(String.format("%s %s%s", getString(R.string.Its), turn.toString(), getString(R.string.Turn)));

            players[0] = new Player(Player.Type.Human);
            players[1] = new Player(Player.Type.Human);
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
     * decides whether to count as a valid XO press, if so, it makes the X / O
     * visible and checks for winner / tie.
     *
     * @param event touch location and info about the touch
    */
    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        final int touchX = (int) event.getRawX();
        final int touchY = (int) event.getRawY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (over_cells[i][j].contains(touchX, touchY) && !cells[i][j].isVisible() && (!vs_multiplayer || (turn == thisType && canPlay))) {
                        boolean isGameOver = play(i, j);
                        if (vs_multiplayer) {
                            writeDatabaseMessage(String.format("turn %s %s", String.valueOf(i), String.valueOf(j)));
                        }
                        if (isGameOver) {
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
    private AlertDialog.Builder winnerAlert(boolean player, String player_won) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.Congratulations))
                .setMessage(player ? String.format("%s %s %s", getString(R.string.Player), player_won, getString(R.string.Won)) : String.format("%s %s", player_won, getString(R.string.Won)))
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
                        if (vs_multiplayer) {
                            writeDatabaseMessage("go");
                            canPlay = lastMessage != null;
                            lastMessage = lastMessage == null ? "" : null;
                        }
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
    private AlertDialog.Builder absoluteWinnerAlert(boolean player, String player_won) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(getString(R.string.Winner))
                .setMessage(player ? String.format("%s %s %s", getString(R.string.Player), player_won, getString(R.string.Won_The_Game)) : String.format("%s %s", player_won, getString(R.string.Won_The_Game)))
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
    @Nullable
    private Type winner() {
        if (checkWinner()) {
            if (timer != 0) {
                counter.cancel();
            }
            Type winner = turn;
            if (winner == Type.O) {
                players[0].won();
                tv_playerOwins.setText(players[0].getWins());
            } else {
                players[1].won();
                tv_playerXwins.setText(players[1].getWins());
            }
            if (!(Integer.parseInt(players[0].getWins()) == maxGames || Integer.parseInt(players[1].getWins()) == maxGames)) {
                winnerAlert(!vs_multiplayer, winner.toString()).show();
                if (vs_multiplayer) {
                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.Its), thisType == turn ? thisName : otherName, getString(R.string.Turn), turn.toString()));
                }
            }
            else {
                if (vs_on_this_device || notCPUturn() || (vs_multiplayer && (winner == thisType))) {
                    if (winner == Type.X) {
                        addXwins();
                    } else if (winner == Type.O) {
                        addOwins();
                    }
                }
                if (vs_multiplayer) {
                    absoluteWinnerAlert(false, thisType == winner ? thisName : otherName).show();
                } else {
                    absoluteWinnerAlert(true, winner.toString()).show();
                }
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
            players[0] = new Player(players[0].playerType);
            players[1] = new Player(players[1].playerType);
            tv_playerOwins.setText("0");
            tv_playerXwins.setText("0");
        }
        hideAll();
        turn = startingType;
        if ((players[0].playerType == Player.Type.CPU && turn == Type.O) || (players[1].playerType == Player.Type.CPU && turn == Type.X)) {
            putCPU();
            turn = turn.flip();
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
            if (vs_multiplayer) {
                writeDatabaseMessage(String.format("turn %s %s", String.valueOf(rand[0]), String.valueOf(rand[1])));
            }
        } else {
            int[] rand = players[1].getRandom();
            while (!cells[rand[0]][rand[1]].setType(turn)) {
                rand = players[1].getRandom();
            }
            if (vs_multiplayer) {
                writeDatabaseMessage(String.format("turn %s %s", String.valueOf(rand[0]), String.valueOf(rand[1])));
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
        if (vs_multiplayer) {
            writeDatabaseMessage(String.format("turn %s %s", String.valueOf(rand[0]), String.valueOf(rand[1])));
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
                if (!cell.isVisible()) {
                    return false;
                }
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
        if (timer != 0)
            counter.cancel();
        cells[i][j].setType(turn);
        if (winner() != null) { //Winner is found
            if (timer != 0)
                counter.cancel();
            return true;
        }                       //No winner is found
        turn = turn.flip(); //Flip the turn
        tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.Its), vs_multiplayer ? (thisType == turn ? thisName : otherName) : turn.toString(), getString(R.string.Turn), turn.toString()));
        if (allVisible()) {     //No winner and the board is full (tie)
            if (timer != 0)
                counter.cancel();
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
            turn = turn.flip();
        }
        if (timer != 0)
            counter.start();
        return false;
    }

    /**
     * This function writes a message to the current lobby in the FireBase database according
     * to this player type (Host / Client)
     *
     * @param message the message to write
     */
    private void writeDatabaseMessage(String message) {
        if (multiType.equals("Host"))
            gameRef.child("hostMessage").setValue(message);
        else
            gameRef.child("clientMessage").setValue(message);
    }

    /**
     * This function initialize all components that require local variables that may be stored
     * in the database
     */
    private void initialize() {

        tv_maxgames.setText(String.format("%s %s %s", getString(R.string.First_To), String.valueOf(maxGames), getString(R.string.Wins_wins)));

        if (timer > 0) {
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
                    if (!vs_multiplayer || turn == thisType) {
                        putCPU(turn == Type.O ? 0 : 1);
                        if (winner() == null) {
                            turn = turn.flip();
                            if (allVisible())
                                tieAlert().show();
                            else if (vs_computer) {
                                putCPU();
                                if (winner() == null) {
                                    turn = turn.flip();
                                    if (allVisible())
                                        tieAlert().show();
                                    else
                                        this.start();
                                }
                            } else {
                                this.start();
                            }
                        }
                    } else {
                        this.start();
                    }
                }
            }.start();
        }
    }

    private void addXwins() {
        Database.dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Database.dataRef.child("Xwins").setValue(dataSnapshot.child("Xwins").getValue(Integer.class) + 1);
                Stats.addXwins();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void addOwins() {
        Database.dataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Database.dataRef.child("Owins").setValue(dataSnapshot.child("Owins").getValue(Integer.class) + 1);
                Stats.addOwins();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * This function gets triggered when this activity is destroyed and
     * calls the 'resign' message to the database so the other player will
     * know that the game is over
     */
    @Override
    protected void onDestroy() {

        if (vs_multiplayer) {
            if (exitStatus == null)
                exitStatus = "exit";
            writeDatabaseMessage(exitStatus);
            gameRef.setValue(null);
        }
        super.onDestroy();
    }

    /**
     * This function gets called when the user pressed the 'back button' and it prevents
     * the user from closing the lobby on multiplayer
     */
    @Override
    public void onBackPressed() {
        if (!vs_multiplayer)
            super.onBackPressed();
    }
}