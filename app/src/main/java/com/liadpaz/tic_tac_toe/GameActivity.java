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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.liadpaz.tic_tac_toe.databinding.ActivityGameBinding;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import static com.liadpaz.tic_tac_toe.Cell.Type;
import static com.liadpaz.tic_tac_toe.Cell.Type.None;
import static com.liadpaz.tic_tac_toe.Cell.Type.O;
import static com.liadpaz.tic_tac_toe.Cell.Type.X;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * This class is the game activity. It contains all the game graphics and mechanics.
 */
public class GameActivity extends AppCompatActivity {

    private Intent musicService;

    private DatabaseReference gameRef;
    private StorageReference storageRef;

    private Type comType;
    private int xWins = 0;
    private int oWins = 0;
    private Type turn;
    private Type thisType;
    private int maxGames;
    private int timer;
    private Type startingType;
    private CountDownTimer counter;

    private boolean difficulty;

    private int topScreen = 0;

    private String thisName;
    private String otherName;
    private String xName;
    private String oName;
    private String lobbyNumber;
    private String multiType;
    private String lastHostMessage;
    private String lastClientMessage;
    private boolean canPlay = true;
    private boolean thisCanPlay = true;
    private Boolean privacy;

    private Cell[][] cells = new Cell[3][3];
    private Rect[][] over_cells = new Rect[3][3];

    private ImageView iv_playerX;
    private ImageView iv_playerO;

    private TextView tv_time_text;
    private TextView tv_timer;
    private TextView tv_playerX;
    private TextView tv_playerXwins;
    private TextView tv_playerO;
    private TextView tv_playerOwins;
    private TextView tv_maxgames;
    private TextView tv_turn;

    private boolean vs_multiplayer;
    private boolean vs_computer;
    private boolean vs_on_this_device;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGameBinding binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        maxGames = getIntent().getIntExtra("Max", -1);
        timer = getIntent().getIntExtra("Timer", -1);
        Utils.Mode mode = (Utils.Mode)getIntent().getSerializableExtra("Mode");
        startingType = (Type)getIntent().getSerializableExtra("Starting");
        lobbyNumber = getIntent().getStringExtra("LobbyNumber");
        multiType = getIntent().getStringExtra("Multiplayer");
        difficulty = getIntent().getBooleanExtra("Difficulty", false);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("music", false)) {
            musicService = new Intent(this, MusicPlayerService.class);
            startService(musicService);
        }

        vs_multiplayer = mode == null;

        turn = startingType;

        ImageView iv_board = binding.ivBoard;
        cells = new Cell[][]{{new Cell(binding.ivTl), new Cell(binding.ivTm), new Cell(binding.ivTr)}, {new Cell(binding.ivMl), new Cell(binding.ivMm), new Cell(binding.ivMr)}, {new Cell(binding.ivBl), new Cell(binding.ivBm), new Cell(binding.ivBr)}};
        binding.btnResign.setOnClickListener(v -> new AlertDialog.Builder(GameActivity.this).setTitle(R.string.resign).setMessage(R.string.resign_message).setPositiveButton(R.string.yes, (dialogInterface, i) -> {
            if (vs_multiplayer) {
                gameRef.removeValue();
            }
            finishAffinity();
            startActivity(new Intent(GameActivity.this, MainActivity.class));
        }).setNegativeButton(R.string.no, null).setCancelable(true).show());
        iv_playerX = binding.ivPlayerX;
        tv_playerX = binding.tvPlayerX;
        tv_playerXwins = binding.tvPlayerXwins;
        iv_playerO = binding.ivPlayerO;
        tv_playerO = binding.tvPlayerO;
        tv_playerOwins = binding.tvPlayerOwins;
        tv_maxgames = binding.tvMaxgames;
        tv_time_text = binding.tvTimeText;
        tv_timer = binding.tvTimer;
        tv_turn = binding.tvTurn;

        if (!vs_multiplayer) {
            binding.toolbarGame.setVisibility(View.VISIBLE);
            setSupportActionBar(binding.toolbarGame);
            getSupportActionBar().setTitle(R.string.game);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            TypedArray ta = obtainStyledAttributes(new int[]{R.attr.actionBarSize});
            topScreen = ta.getDimensionPixelSize(0, -1);
            ta.recycle();
            iv_board.setTop(-topScreen);

            vs_computer = mode == Utils.Mode.Computer;
            vs_on_this_device = mode == Utils.Mode.TwoPlayer;
            initialize();
        } else {
            gameRef = Firebase.dataRef.child("Lobbies").child(lobbyNumber);
            gameRef.onDisconnect().removeValue();
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

                    if ("X".equals(dataSnapshot.child("hostType").getValue(String.class))) {
                        thisType = "Host".equals(multiType) ? X : O;
                        xName = dataSnapshot.child("hostName").getValue(String.class);
                        oName = dataSnapshot.child("clientName").getValue(String.class);

                        thisName = "Host".equals(multiType) ? xName : oName;
                        otherName = "Host".equals(multiType) ? oName : xName;
                    } else {
                        thisType = multiType.equals("Host") ? O : X;
                        xName = dataSnapshot.child("clientName").getValue(String.class);
                        oName = dataSnapshot.child("hostName").getValue(String.class);

                        thisName = "Host".equals(multiType) ? oName : xName;
                        otherName = "Host".equals(multiType) ? xName : oName;
                    }

                    timer = dataSnapshot.child("timer").getValue(Integer.class);
                    maxGames = dataSnapshot.child("max").getValue(Integer.class);

                    tv_playerO.setText(oName);
                    tv_playerX.setText(xName);
                    tv_playerX.setVisibility(View.VISIBLE);
                    tv_playerO.setVisibility(View.VISIBLE);
                    tv_playerXwins.setVisibility(View.VISIBLE);
                    tv_playerOwins.setVisibility(View.VISIBLE);

                    startingType = "X".equals(dataSnapshot.child("startingType").getValue(String.class)) ? X : O;
                    turn = startingType;
                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == startingType ? thisName : otherName, getString(R.string.turn), startingType.toString()));

                    initialize();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
            gameRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChildren()) {
                        try {
                            addWins(thisType);
                            absoluteWinnerAlert(false, thisName).show();
                            return;
                        } catch (Exception ignored) {
                            return;
                        }
                    }
                    if ("Host".equals(multiType)) {
                        String message = dataSnapshot.child("clientMessage").getValue(String.class);
                        if (lastClientMessage == null) {
                            lastClientMessage = message;
                        }
                        if (message != null && !message.equals(lastClientMessage)) {
                            String[] messages = message.split(" ");
                            switch (messages[0]) {
                                case "turn": {
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;
                                }

                                case "go": {
                                    turn = startingType;
                                    if (timer != 0 && thisCanPlay) {
                                        counter.start();
                                    }
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == startingType ? thisName : otherName, getString(R.string.turn), startingType.toString()));
                                    canPlay = true;
                                    break;
                                }
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
                                case "turn": {
                                    play(Integer.parseInt(messages[1]), Integer.parseInt(messages[2]));
                                    break;
                                }

                                case "go": {
                                    turn = startingType;
                                    if (timer != 0 && thisCanPlay) {
                                        counter.start();
                                    }
                                    tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == turn ? thisName : otherName, getString(R.string.turn), turn.toString()));
                                    canPlay = true;
                                    break;
                                }
                            }
                        }
                        lastHostMessage = message;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }

        tv_playerXwins.setText("0");
        tv_playerOwins.setText("0");

        DisplayMetrics screen = new DisplayMetrics();
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
            new AlertDialog.Builder(this).setCancelable(false).setMessage(R.string.player_chooser).setTitle(R.string.choose_player).setPositiveButton("X", (dialog, which) -> {
                thisType = X;
                comType = O;
                tv_playerO.setText(R.string.computer);
                tv_playerX.setText(R.string.you);
                tv_playerX.setVisibility(View.VISIBLE);
                tv_playerO.setVisibility(View.VISIBLE);
                tv_playerXwins.setVisibility(View.VISIBLE);
                tv_playerOwins.setVisibility(View.VISIBLE);
                if (startingType == O) {
                    putCPU();
                }
                if (timer != 0) {
                    counter.start();
                }
            }).setNegativeButton("O", (dialog, which) -> {
                thisType = O;
                comType = X;
                tv_playerO.setText(R.string.you);
                tv_playerX.setText(R.string.computer);
                tv_playerX.setVisibility(View.VISIBLE);
                tv_playerO.setVisibility(View.VISIBLE);
                tv_playerXwins.setVisibility(View.VISIBLE);
                tv_playerOwins.setVisibility(View.VISIBLE);
                if (startingType == X) {
                    putCPU();
                }
                if (timer != 0) {
                    counter.start();
                }
            }).show();

        } else if (vs_on_this_device) {
            tv_turn.setText(String.format("%s %s%s", getString(R.string.its), turn.toString(), getString(R.string.turn)));
            tv_playerO.setText("O");
            tv_playerX.setText("X");
            tv_playerX.setVisibility(View.VISIBLE);
            tv_playerO.setVisibility(View.VISIBLE);
            tv_playerXwins.setVisibility(View.VISIBLE);
            tv_playerOwins.setVisibility(View.VISIBLE);
        }
    }

    /**
     * This function gets triggered whenever the user touches the screen, and decides whether to
     * count as a valid XO press, if so, it makes the X / O visible and checks for winner / tie.
     *
     * @param event touch location and info about the touch
     */
    @SuppressLint("DefaultLocale")
    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        final int touchX = (int)event.getRawX();
        final int touchY = (int)event.getRawY();
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
    @NonNull
    private AlertDialog.Builder tieAlert() {
        thisCanPlay = false;
        return new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string.tie).setMessage(R.string.its_a_tie).setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
            thisCanPlay = true;
            resetGame();
            if (vs_multiplayer) {
                writeDatabaseMessage("go");
                tv_turn.setText(String.format("%s %s%s (%s)", getString(R.string.its), thisType == turn ? thisName : otherName, getString(R.string.turn), turn.toString()));
            } else {
                tv_turn.setText(String.format("%s %s%s", getString(R.string.its), turn.toString(), getString(R.string.turn)));
            }
            if (timer != 0 && (turn == thisType || (vs_multiplayer && canPlay))) {
                if (!vs_multiplayer || canPlay) {
                    counter.start();
                }
            }
        });
    }

    /**
     * This function builds and returns a winner AlertDialog with the winner name in it
     *
     * @param player_won player_won: a String contains the player name
     * @return winner AlertDialog
     */
    @NonNull
    private AlertDialog.Builder winnerAlert(boolean player, String player_won) {
        thisCanPlay = false;
        return new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string.congratulations).setMessage(player ? String.format("%s %s %s", getString(R.string.player), player_won, getString(R.string.won)) : String.format("%s %s", player_won, getString(R.string.won))).setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
            resetGame();
            thisCanPlay = true;
            if (vs_multiplayer) {
                writeDatabaseMessage("go");
            }
            if (timer != 0 && (turn == thisType || (vs_multiplayer && canPlay))) {
                if (!vs_multiplayer || canPlay) {
                    counter.start();
                }
            }
        });
    }

    /**
     * This function builds a winner dialog
     *
     * <p>This function builds and returns an absolute winner AlertDialog with the winner name in
     * it</p>
     *
     * @param player     true to show 'player' at the start
     * @param player_won a String contains the player name
     * @return absolute winner AlertDialog
     */
    @NonNull
    private AlertDialog.Builder absoluteWinnerAlert(boolean player, String player_won) {
        return new AlertDialog.Builder(this).setCancelable(false).setTitle(R.string.winner).setMessage(player ? String.format("%s %s %s", getString(R.string.player), player_won, getString(R.string.won_the_game)) : String.format("%s %s", player_won, getString(R.string.won_the_game))).setPositiveButton(R.string.continue_dialog, (dialog, which) -> {
            finishAffinity();
            startActivity(new Intent(GameActivity.this, MainActivity.class));
        });
    }

    /**
     * This function checks if there is a winner
     *
     * <p>This function checks if there is a winner and returns the type of the
     * winner if there is, otherwise returns null</p>
     */
    private void winner(Type winner) {
        if (winner == O) {
            oWins++;
        } else {
            xWins++;
        }
        tv_playerOwins.setText(String.valueOf(oWins));
        tv_playerXwins.setText(String.valueOf(xWins));
        if (!(oWins == maxGames || xWins == maxGames)) {
            winnerAlert(!vs_multiplayer, winner.toString()).show();
        } else {
            if (vs_on_this_device || winner == thisType) {
                addWins(winner);
            }
            absoluteWinnerAlert(!vs_multiplayer, vs_multiplayer ? (thisType == winner ? thisName : otherName) : winner.toString()).show();
        }
    }

    /**
     * This function resets the game with or without players score
     */
    private void resetGame() {
        hideAll();
        turn = startingType;
        if ((comType == O && turn == O) || (comType == X && turn == X)) {
            putCPU();
        }
        if (timer != 0) {
            counter.start();
        }
    }

    /**
     * This function plays a CPU turn
     */
    @SuppressLint("DefaultLocale")
    private void putCPU() {
        if (difficulty) {    // hard difficulty
            int bestScore = Integer.MIN_VALUE;
            int[] move = new int[0];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!cells[i][j].isVisible()) {
                        cells[i][j].setInvisibleType(comType);
                        int score = minimax(cells, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, false);
                        cells[i][j].hide();
                        if (score > bestScore) {
                            bestScore = score;
                            move = new int[]{i, j};
                        }
                    }
                }
            }
            cells[move[0]][move[1]].setType(comType);
        } else {  // easy difficulty
            Random random = new Random();
            int[] rand = new int[]{random.nextInt(3), random.nextInt(3)};
            while (!cells[rand[0]][rand[1]].setType(turn)) {
                rand = new int[]{random.nextInt(3), random.nextInt(3)};
            }
        }
        turn = comType.flip();
    }

    private int minimax(@NotNull Cell[][] board, int depth, int alpha, int beta, boolean isMaximizing) {
        Type result;
        if ((result = checkWinner(board)) != null) {
            if (result == None) {
                return 0;
            }
            return result == comType ? 10 - depth : depth - 10;
        }

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!cells[i][j].isVisible()) {
                        cells[i][j].setInvisibleType(comType);
                        int score = minimax(board, depth + 1, alpha, beta, false);
                        cells[i][j].hide();
                        bestScore = max(bestScore, score);
                        alpha = max(alpha, score);
                        if (beta <= alpha) {
                            return bestScore;
                        }
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!cells[i][j].isVisible()) {
                        cells[i][j].setInvisibleType(comType.flip());
                        int score = minimax(board, depth + 1, alpha, beta, true);
                        cells[i][j].hide();
                        bestScore = min(bestScore, score);
                        beta = min(beta, score);
                        if (beta <= alpha) {
                            return bestScore;
                        }
                    }
                }
            }
            return bestScore;
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
        for (Cell[] cellRow : cells) {
            for (Cell cell : cellRow) {
                cell.hide();
            }
        }
    }

    /**
     * This function checks if there is a winner
     *
     * @return true if there is a winner, otherwise false
     */
    @Nullable
    private Type checkWinner(@NotNull Cell[][] cells) {
        Type winner = null;
        for (int i = 0; i < 3; i++) {
            if (cells[i][0].getType() == cells[i][1].getType() && cells[i][1].getType() == cells[i][2].getType() && cells[i][0].getType() != None) {
                winner = cells[i][0].getType();
            }
            if (cells[0][i].getType() == cells[1][i].getType() && cells[1][i].getType() == cells[2][i].getType() && cells[0][i].getType() != None) {
                winner = cells[0][i].getType();
            }
        }
        if ((cells[0][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[2][2].getType() && cells[0][0].getType() != None) || (cells[2][0].getType() == cells[1][1].getType() && cells[1][1].getType() == cells[0][2].getType() && cells[2][0].getType() != None)) {
            winner = cells[1][1].getType();
        }
        return (winner == null && allVisible()) ? None : winner;
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
        Type win;
        if ((win = checkWinner(cells)) != null && win != None) { // Winner is found
            winner(win);
            if (vs_multiplayer) {
                canPlay = false;
            }
            return;
        }
        if (win == None) {     // No winner and the board is full (tie)
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
        if (vs_computer) {      // One of the players is CPU
            putCPU();
            cancelCounter();
            if ((win = checkWinner(cells)) != null && win != None) {
                winner(win);
                return;
            }
            if (win == None) {
                tieAlert().show();
                return;
            }
        }
        if (timer != 0) {
            counter.start();
        }
    }

    /**
     * This function writes a message to the current lobby in the FireBase database according to
     * this player type (Host / Client)
     *
     * @param message the message to write
     */
    private void writeDatabaseMessage(String message) {
        gameRef.child(multiType.equals("Host") ? "hostMessage" : "clientMessage").setValue(message);
    }

    /**
     * This function initialize all components that require local variables that may be stored in
     * the database
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
                    tv_timer.setText(String.format("%s %s", String.valueOf((int)millisUntilFinished / 1000 + 1), getString(R.string.seconds)));
                }

                @Override
                public void onFinish() {
                    if (!vs_multiplayer || (turn == thisType && canPlay)) {
                        Random random = new Random();
                        int[] rand = new int[]{random.nextInt(3), random.nextInt(3)};
                        while (cells[rand[0]][rand[1]].isVisible()) {
                            rand = new int[]{random.nextInt(3), random.nextInt(3)};
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
    private void addWins(Type type) {
        if (type == X) {
            Stats.addXwins();
        } else {
            Stats.addOwins();
        }
    }

    /**
     * This function puts the players' photos in their respective places
     */
    private void putPhotos() {
        final File remotePhoto = new File(getFilesDir(), "PhotoRemote.jpg");
        storageRef.child(multiType.equals("Host") ? "Client" : "Host").getFile(remotePhoto).addOnCompleteListener(task -> {
            Utils.remotePhotoUri = FileProvider.getUriForFile(GameActivity.this, "com.liadpaz.tic_tac_toe.fileprovider", remotePhoto);
            if (thisType == X) {
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
     * This function gets called when the user pressed the 'back button' and it prevents the user
     * from closing the lobby on multiplayer
     */
    @Override
    public void onBackPressed() {
        if (!vs_multiplayer) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (musicService != null) {
            stopService(musicService);
        }
        if (vs_multiplayer) {
            gameRef.removeValue();
        }
        super.onDestroy();
    }
}