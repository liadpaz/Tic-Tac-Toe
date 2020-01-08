package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
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
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class Game extends AppCompatActivity {

    DatabaseReference gameRef;
    StorageReference storageRef;

    Cell.Type turn;
    Cell.Type thisType;
    int maxGames;
    int timer;
    Utils.Mode mode;
    Cell.Type startingType;
    CountDownTimer counter;
    boolean difficulty;

    int topScreen = 0;
    DisplayMetrics screen;

    String thisName;
    String otherName;
    String Xname;
    String Oname;
    String lobbyNumber;
    String multiType;
    String lastHostMessage;
    String lastClientMessage;
    String lastSentMessage;
    boolean canPlay = true;
    boolean thisCanPlay = true;
    Boolean privacy;

    ImageView iv_board;
    Player[] players = new Player[2];
    Cell[][] cells = new Cell[3][3];
    Rect[][] over_cells = new Rect[3][3];

    ImageView iv_playerX;
    ImageView iv_playerO;

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
        difficulty = getIntent().getBooleanExtra("Difficulty", false);

        vs_multiplayer = mode == null;

        turn = startingType;

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
        iv_playerX = findViewById(R.id.iv_playerX);
        tv_playerX = findViewById(R.id.tv_playerX);
        tv_playerXwins = findViewById(R.id.tv_playerXwins);
        iv_playerO = findViewById(R.id.iv_playerO);
        tv_playerO = findViewById(R.id.tv_playerO);
        tv_playerOwins = findViewById(R.id.tv_playerOwins);
        tv_maxgames = findViewById(R.id.tv_maxgames);
        tv_time_text = findViewById(R.id.tv_time_text);
        tv_timer = findViewById(R.id.tv_timer);
        tv_turn = findViewById(R.id.tv_turn);

        if (!vs_multiplayer) {
            findViewById(R.id.toolbar_game).setVisibility(View.VISIBLE);
            setSupportActionBar(findViewById(R.id.toolbar_game));
            Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.game);
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            TypedArray ta = obtainStyledAttributes(new int[]{R.attr.actionBarSize});
            topScreen = ta.getDimensionPixelSize(0, -1);
            ta.recycle();
            iv_board.setTop(-topScreen);

            vs_computer = mode == Utils.Mode.Computer;
            vs_on_this_device = mode == Utils.Mode.TwoPlayer;
            initialize();
        } else {
            btn_reset.setVisibility(View.INVISIBLE);

            gameRef = Firebase.dataRef.child("Lobbies").child(lobbyNumber);
            gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (privacy == null) {
                        privacy = Objects.requireNonNull(dataSnapshot.child("privacy").getValue(Boolean.class));
                        if (!privacy) {
                            storageRef = FirebaseStorage.getInstance().getReference().child("Lobbies").child(lobbyNumber);
                            putPhotos();
                        }
                    }

                    if (Objects.equals(dataSnapshot.child("hostType").getValue(String.class), "X")) {
                        thisType = multiType.equals("Host") ? Cell.Type.X : Cell.Type.O;
                        Xname = dataSnapshot.child("hostName").getValue(String.class);
                        Oname = dataSnapshot.child("clientName").getValue(String.class);

                        thisName = multiType.equals("Host") ? Xname : Oname;
                        otherName = multiType.equals("Host") ? Oname : Xname;
                    } else {
                        thisType = multiType.equals("Host") ? Cell.Type.O : Cell.Type.X;
                        Xname = dataSnapshot.child("clientName").getValue(String.class);
                        Oname = dataSnapshot.child("hostName").getValue(String.class);

                        thisName = multiType.equals("Host") ? Oname : Xname;
                        otherName = multiType.equals("Host") ? Xname : Oname;
                    }

                    timer = Objects.requireNonNull(dataSnapshot.child("timer").getValue(Integer.class));
                    maxGames = Objects.requireNonNull(dataSnapshot.child("max").getValue(Integer.class));

                    players[0] = new Player(Player.Type.Human);
                    players[1] = new Player(Player.Type.Human);
                    tv_playerO.setText(Oname);
                    tv_playerX.setText(Xname);
                    tv_playerX.setVisibility(View.VISIBLE);
                    tv_playerO.setVisibility(View.VISIBLE);
                    tv_playerXwins.setVisibility(View.VISIBLE);
                    tv_playerOwins.setVisibility(View.VISIBLE);

                    startingType = Objects.equals(dataSnapshot.child("startingType").getValue(String.class), "X") ? Cell.Type.X : Cell.Type.O;
                    turn = startingType;
                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == startingType ? thisName : otherName, getString(R.string.turn), startingType.toString()));

                    initialize();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            gameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (multiType.equals("Host")) {
                        String message = dataSnapshot.child("clientMessage").getValue(String.class);
                        if (lastClientMessage == null) {
                            lastClientMessage = message;
                        }
                        if (message != null && !message.equals(lastClientMessage)) {
                            String[] messages = message.split(" ");
                            switch (messages[0]) {
                                case "turn":
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;

                                case "left":
                                    if (thisType == Cell.Type.X) {
                                        Stats.addXwins();
                                    } else {
                                        Stats.addOwins();
                                    }
                                    gameRef.removeValue();
                                    absoluteWinnerAlert(false, thisName).show();
                                    break;

                                case "go":
                                    turn = startingType;
                                    if (timer != 0 && thisCanPlay) {
                                        counter.start();
                                    }
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == startingType ? thisName : otherName, getString(R.string.turn), startingType.toString()));
                                    canPlay = true;
                                    break;
                            }
                        }
                        lastClientMessage = message;
                    } else {
                        String message = dataSnapshot.child("hostMessage").getValue(String.class);
                        if (lastHostMessage == null) {
                            lastHostMessage = message;
                        }
                        if (message != null && !message.equals(lastHostMessage)) {
                            String[] messages = message.split(" ");
                            switch (messages[0]) {
                                case "turn":
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;

                                case "left":
                                    if (thisType == Cell.Type.X) {
                                        Stats.addXwins();
                                    } else {
                                        Stats.addOwins();
                                    }
                                    gameRef.removeValue();
                                    absoluteWinnerAlert(false, thisName).show();
                                    break;

                                case "go":
                                    turn = startingType;
                                    if (timer != 0 && thisCanPlay) {
                                        counter.start();
                                    }
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == turn ? thisName : otherName, getString(R.string.turn), turn.toString()));
                                    canPlay = true;
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

        btn_resign.setOnClickListener(v -> new AlertDialog.Builder(Game.this)
                .setTitle(R.string.resign)
                .setMessage(R.string.resign_message)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    if (vs_multiplayer) {
                        Game.this.writeDatabaseMessage("left");
                    }
                    Game.this.finishAffinity();
                    Game.this.startActivity(new Intent(Game.this, MainActivity.class));
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .show());
        btn_reset.setOnClickListener(v -> new AlertDialog.Builder(Game.this)
                .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                    tv_playerOwins.setText("0");
                    tv_playerXwins.setText("0");
                    Game.this.resetGame(true);
                })
                .setNegativeButton(R.string.no, null)
                .setCancelable(true)
                .setTitle(R.string.restart)
                .setMessage(R.string.restart_question)
                .show());

        screen = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(screen);
        int x = screen.widthPixels;

        iv_board.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));
        iv_board.setX(0);
        iv_board.setY(0);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                over_cells[i][j] = new Rect(j * (x / 3), i * (x / 3) + topScreen, (j + 1) * (x / 3), (i + 1) * (x / 3) + topScreen);
                cells[i][j].setSize(x / 3);
                cells[i][j].setLocation(j * (x / 3), i * (x / 3) + topScreen);
            }
        }

        if (vs_computer) {
            cancelCounter();
            tv_turn.setVisibility(View.INVISIBLE);
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(R.string.player_chooser)
                    .setTitle(R.string.choose_player)
                    .setPositiveButton("X", (dialog, which) -> {
                        players[0] = new Player(Player.Type.CPU);
                        players[1] = new Player(Player.Type.Human);
                        tv_playerO.setText(R.string.computer);
                        tv_playerX.setText(R.string.you);
                        tv_playerX.setVisibility(View.VISIBLE);
                        tv_playerO.setVisibility(View.VISIBLE);
                        tv_playerXwins.setVisibility(View.VISIBLE);
                        tv_playerOwins.setVisibility(View.VISIBLE);
                        if (startingType == Cell.Type.O) {
                            Game.this.putCPU();
                            turn = Cell.Type.X;
                        }
                        if (timer != 0) {
                            counter.start();
                        }
                    })
                    .setNegativeButton("O", (dialog, which) -> {
                        players[0] = new Player(Player.Type.Human);
                        players[1] = new Player(Player.Type.CPU);
                        tv_playerO.setText(R.string.you);
                        tv_playerX.setText(R.string.computer);
                        tv_playerX.setVisibility(View.VISIBLE);
                        tv_playerO.setVisibility(View.VISIBLE);
                        tv_playerXwins.setVisibility(View.VISIBLE);
                        tv_playerOwins.setVisibility(View.VISIBLE);
                        if (startingType == Cell.Type.X) {
                            Game.this.putCPU();
                            turn = Cell.Type.O;
                        }
                        if (timer != 0) {
                            counter.start();
                        }
                    })
                    .show();
        } else if (vs_on_this_device) {
            tv_turn.setText(String.format("%s %s%s", getString(R.string.its), turn.toString(), getString(R.string.turn)));

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
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        final int touchX = (int) event.getRawX();
        final int touchY = (int) event.getRawY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (over_cells[i][j].contains(touchX, touchY) && !cells[i][j].isVisible() && (!vs_multiplayer || (turn == thisType && canPlay && thisCanPlay))) {
                        play(i, j);
                        if (vs_multiplayer) {
                            writeDatabaseMessage(String.format("turn %d %d", i, j));
                        }
                        i = 3;
                        break;
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * This function builds and returns a tie AlertDialog
     *
     * @return tie AlertDialog
     */
    private AlertDialog.Builder tieAlert() {
        thisCanPlay = false;
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.tie)
                .setMessage(R.string.its_a_tie)
                .setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
                    thisCanPlay = true;
                    Game.this.resetGame(false);
                    if (vs_multiplayer) {
                        Game.this.writeDatabaseMessage("go");
                        tv_turn.setText(String.format("%s %s%s (%s)", Game.this.getString(R.string.its), thisType == turn ? thisName : otherName, Game.this.getString(R.string.turn), turn.toString()));
                    } else {
                        tv_turn.setText(String.format("%s %s%s", Game.this.getString(R.string.its), turn.toString(), Game.this.getString(R.string.turn)));
                    }
                    if (timer != 0 && (Game.this.notCPUturn() || (vs_multiplayer && canPlay))) {
                        if (!vs_multiplayer || canPlay) {
                            counter.start();
                        }
                    }
                });
    }

    /**
     * This function builds and returns a winner AlertDialog with the winner name in it
     *
     * @param player_won    player_won: a String contains the player name
     *
     * @return              winner AlertDialog
     */
    private AlertDialog.Builder winnerAlert(boolean player, String player_won) {
        thisCanPlay = false;
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.congratulations)
                .setMessage(player ? String.format("%s %s %s", getString(R.string.player), player_won, getString(R.string.won)) : String.format("%s %s", player_won, getString(R.string.won)))
                .setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
                    Game.this.resetGame(false);
                    thisCanPlay = true;
                    if (vs_multiplayer) {
                        Game.this.writeDatabaseMessage("go");
                    }
                    if (timer != 0 && (Game.this.notCPUturn() || (vs_multiplayer && canPlay))) {
                        if (!vs_multiplayer || canPlay) {
                            counter.start();
                        }
                    }
                });
    }

    /**
     * This function builds a winner dialog
     *
     * <p>This function builds and returns an absolute winner AlertDialog with the winner name in it</p>
     *
     * @param player        true to show 'player' at the start
     * @param player_won    a String contains the player name
     *
     * @return              absolute winner AlertDialog
     */
    private AlertDialog.Builder absoluteWinnerAlert(boolean player, String player_won) {
        return new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.winner)
                .setMessage(player ? String.format("%s %s %s", getString(R.string.player), player_won, getString(R.string.won_the_game)) : String.format("%s %s", player_won, getString(R.string.won_the_game)))
                .setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
                    Game.this.finishAffinity();
                    Game.this.startActivity(new Intent(Game.this, MainActivity.class));
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
    private boolean winner() {
        if (checkWinner()) {
            Cell.Type winner = turn;
            players[winner == Cell.Type.O ? 0 : 1].won();
            tv_playerOwins.setText(String.valueOf(players[0].getWins()));
            tv_playerXwins.setText(String.valueOf(players[1].getWins()));
            if (!(players[0].getWins() == maxGames || players[1].getWins() == maxGames)) {
                winnerAlert(!vs_multiplayer, winner.toString()).show();
            } else {
                if (vs_on_this_device || notCPUturn() || (vs_multiplayer && (winner == thisType))) {
                    addWins(winner);
                }
                absoluteWinnerAlert(!vs_multiplayer, vs_multiplayer ? (thisType == winner ? thisName : otherName) : winner.toString()).show();
            }
            return true;
        }
        return false;
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
        if ((players[0].playerType == Player.Type.CPU && turn == Cell.Type.O) || (players[1].playerType == Player.Type.CPU && turn == Cell.Type.X)) {
            putCPU();
            turn = turn.flip();
        }
    }

    /**
     * This function plays a CPU turn
     */
    @SuppressLint("DefaultLocale")
    private void putCPU() {
        int[] rand = Utils.getRandom();
        while (!cells[rand[0]][rand[1]].setType(turn)) {
            rand = Utils.getRandom();
        }
        if (vs_multiplayer) {
            writeDatabaseMessage(String.format("turn %d %d", rand[0], rand[1]));
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
        return (!vs_computer || ((turn != Cell.Type.O || players[0].playerType != Player.Type.CPU) && (turn != Cell.Type.X || players[1].playerType != Player.Type.CPU)));
    }

    /**
     * This function plays a turn, if needed a CPU turn afterwards it does this
     *
     * @param i the y index of the cell
     * @param j the x index of the cell
     */
    private void play(int i, int j) {
        cancelCounter();
        cells[i][j].setType(turn);
        if (winner()) { //Winner is found
            if (vs_multiplayer) {
                canPlay = false;
            }
            return;
        }
        if (allVisible()) {     //No winner and the board is full (tie)
            cancelCounter();
            if (vs_multiplayer) {
                canPlay = false;
            }
            tieAlert().show();
            return;
        }
        turn = turn.flip();
        if (vs_multiplayer) {
            tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == turn ? thisName : otherName, getString(R.string.turn), turn.toString()));
        } else {
            tv_turn.setText(String.format("%s %s%s", getString(R.string.its), turn.toString(), getString(R.string.turn)));
        }
        if (vs_computer) {      //One of the players is CPU
            putCPU();
            cancelCounter();
            if (winner()) {
                return;
            }
            if (allVisible()) {
                tieAlert().show();
                return;
            }
            turn = turn.flip();
        }
        if (timer != 0) {
            counter.start();
        }
    }

    /**
     * This function writes a message to the current lobby in the FireBase database according
     * to this player type (Host / Client)
     *
     * @param message the message to write
     */
    private void writeDatabaseMessage(String message) {
        lastSentMessage = message;
        gameRef.child(multiType.equals("Host") ? "hostMessage" : "clientMessage").setValue(message);
    }

    /**
     * This function initialize all components that require local variables that may be stored
     * in the database
     */
    @SuppressLint("DefaultLocale")
    private void initialize() {
        tv_maxgames.setText(String.format("%s %s %s", getString(R.string.first_to), String.valueOf(maxGames), getString(R.string.wins_wins)));

        if (timer != 0) {
            tv_time_text.setVisibility(View.VISIBLE);
            tv_timer.setVisibility(View.VISIBLE);
            tv_timer.setText(String.format("%s %s", String.valueOf(timer), getString(R.string.seconds)));
            counter = new CountDownTimer(timer * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    tv_timer.setText(String.format("%s %s", String.valueOf((int) millisUntilFinished / 1000 + 1), getString(R.string.seconds)));
                }

                @Override
                public void onFinish() {
                    if (!vs_multiplayer || (turn == thisType && canPlay)) {
                        int[] rand = Utils.getRandom();
                        while (cells[rand[0]][rand[1]].isVisible()) {
                            rand = Utils.getRandom();
                        }
                        play(rand[0], rand[1]);
                        if (vs_multiplayer) {
                            writeDatabaseMessage(String.format("turn %d %d", rand[0], rand[1]));
                        }
                    }
                }
            }.start();
        }
    }

    /**
     * This function cancels the counter if should
     */
    private void cancelCounter() {
        if (timer != 0) {
            counter.cancel();
        }
    }

    /**
     * This function adds wins to the local and global wins
     *
     * @param type the type of winner
     */
    private void addWins(Cell.Type type) {
        if (type == Cell.Type.X) {
            Stats.addXwins();
        } else {
            Stats.addOwins();
        }
    }

    /**
     * This function places the multiplayer photos in their respective places
     */
    private void putPhotos() {
        final File remotePhoto = new File(getFilesDir(), "Photo1.jpg");
        storageRef.child(multiType.equals("Host") ? "Client" : "Host").getFile(remotePhoto).addOnCompleteListener(task -> {
            Utils.remotePhotoUri = FileProvider.getUriForFile(Game.this, "com.liadpaz.tic_tac_toe.fileprovider", remotePhoto);
            if (thisType == Cell.Type.X) {
                iv_playerX.setImageURI(Utils.localPhotoUri);
                iv_playerX.setVisibility(View.VISIBLE);
                iv_playerO.setImageURI(Utils.remotePhotoUri);
                iv_playerO.setVisibility(View.VISIBLE);
            } else {
                iv_playerX.setImageURI(Utils.remotePhotoUri);
                iv_playerX.setVisibility(View.VISIBLE);
                iv_playerO.setImageURI(Utils.localPhotoUri);
                iv_playerO.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * This function gets called when the user pressed the 'back button' and it prevents
     * the user from closing the lobby on multiplayer
     */
    @Override
    public void onBackPressed() {
        if (!vs_multiplayer) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (vs_multiplayer && lastSentMessage != null && !lastSentMessage.equals("left")) {
            gameRef.removeValue();
        }
        super.onDestroy();
    }
}