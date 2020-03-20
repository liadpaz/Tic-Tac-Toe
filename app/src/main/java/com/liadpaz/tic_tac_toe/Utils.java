package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.liadpaz.tic_tac_toe.Cell.Type.None;
import static com.liadpaz.tic_tac_toe.Cell.Type.X;

/**
 * This class is for general utilities
 */
class Utils {
    static Uri localPhotoUri;
    static Uri remotePhotoUri;

    private static ArrayList<Integer> lobbiesInt = IntStream.range(0, 10000).boxed().collect(Collectors.toCollection(ArrayList::new));
    private static ArrayList<String> lobbies;

    /**
     * This variable is the last time that was recorded
     */
    private static long last_time;

    /**
     * This function sets the {@code last_time} variable to the current time
     */
    static void setTime() {
        last_time = Calendar.getInstance().getTime().getTime();
    }

    /**
     * This function calculates the time between the {@code last_time} variable to the current time
     *
     * @return the time difference
     */
    static long getTime() {
        long time = (Calendar.getInstance().getTime().getTime() - last_time) / 1000;
        setTime();
        return time;
    }

    /**
     * This function generates 4 random integers and concatenates them to one String
     *
     * @return String containing 4 random integers
     */
    static ArrayList<String> getRoomNumber() {
        if (lobbies == null) {
            lobbies = new ArrayList<>();
            for (int number : lobbiesInt) {
                if (number < 1000) {
                    if (number < 100) {
                        if (number < 10) {
                            lobbies.add("000" + number);
                            continue;
                        }
                        lobbies.add("00" + number);
                        continue;
                    }
                    lobbies.add("0" + number);
                    continue;
                }
                lobbies.add(String.valueOf(number));
            }
        }
        Collections.shuffle(lobbies);
        return lobbies;
    }

    /**
     * This enum is to differ a 'TwoPlayer', 'Computer' or Multiplayer game mode
     */
    public enum Mode {TwoPlayer, Computer, Multiplayer}
}

/**
 * This class contains the main reference to the Firebase Firebase and Firebase Storage
 */
class Firebase {
    /**
     * The main reference to the Firebase Database
     */
    static final DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
    /**
     * The main reference to the Firebase Storage
     */
    static final StorageReference storeRef = FirebaseStorage.getInstance().getReference();
    /**
     * The reference to the user in the database if a user is connected, otherwise null
     */
    static DatabaseReference userRef;
}

/**
 * This class represents the game cell
 */
class Cell {
    private final ImageView XO;
    private Type type;
    private boolean visible = false;

    /**
     * Constructor, attaches 2 ImageViews (X, O) to the cell
     *
     * @param XO an array of 2 ImageVies (X, O)
     */
    Cell(ImageView XO) {
        this.XO = XO;
        this.type = None;
    }

    /**
     * This function checks if the cell is visible to the player/s
     *
     * @return true if visible, otherwise false
     */
    boolean isVisible() {
        return visible;
    }

    /**
     * This function sets the cell type to the {@code type} param
     *
     * @param type the type to set the cell as
     * @return true if the function have done the transformation, otherwise false
     */
    boolean setType(Type type) {
        if (!visible) {
            this.type = type;
            if (type == X) {
                XO.setImageResource(R.drawable.x);
                XO.setVisibility(View.VISIBLE);
            } else {/*if (type == Type.O)*/
                XO.setImageResource(R.drawable.o);
                XO.setVisibility(View.VISIBLE);
            }
            visible = true;
            return true;
        }
        return false;
    }

    /**
     * This function sets the cell type to the {@code type} type without the visual representation
     * in order not to overload the ui thread work on the minimax algorithm.
     *
     * @param type the type to set the cell to
     */
    void setInvisibleType(Type type) {
        this.type = type;
        visible = true;
    }

    /**
     * This function returns the cell's type
     *
     * @return the cell's type
     */
    Type getType() {
        return type;
    }

    /**
     * This function sets the cell location on the screen
     *
     * @param x the x coordinates
     * @param y the y coordinates
     */
    void setLocation(int x, int y) {
        XO.setX(x);
        XO.setY(y);
    }

    /**
     * This function sets the cell size
     *
     * @param x the height and width of the cell
     */
    void setSize(int x) {
        XO.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));
    }

    /**
     * This function hides the cell from the player/s and sets his type to null
     */
    void hide() {
        visible = false;
        type = None;
        XO.setVisibility(View.GONE);
    }

    /**
     * This is the type of the cell. eg. X, O
     */
    public enum Type {
        None, X, O;

        /**
         * This function flips the type. eg. X to O and O to X
         *
         * @return X if the type was O, and O if the type was X
         */
        Type flip() {
            if (this == X) {
                return O;
            }
            return X;
        }
    }
}

/**
 * This class is for the statistics
 */
class Stats {

    /**
     * The app's shared preferences
     */
    private static SharedPreferences sharedPreferences;

    /**
     * Constructor for an instance of the Stats class, sets the static shared preferences to the
     * parameter
     *
     * @param sharedPreferences the shared preferences to set
     */
    Stats(SharedPreferences sharedPreferences) {
        Stats.sharedPreferences = sharedPreferences;
    }

    /**
     * This function returns the privacy state from the shared preferences
     *
     * @return privacy state from the shared preferences
     */
    static boolean readPrivacy() {
        return sharedPreferences.getBoolean("privacy", false);
    }

    /**
     * This function return whether the user prefer to use his google name in multiplayer or not
     *
     * @return true if the user prefer to his google name in multiplayer or not
     */
    static boolean getGoogleName() {
        return sharedPreferences.getBoolean("google_name", false);
    }

    /**
     * This function sets the user preference of his name in multiplayer game (google name or
     * custom)
     *
     * @param googleName true if the user prefer to use his google name
     */
    static void setGoogleName(boolean googleName) {
        sharedPreferences.edit().putBoolean("google_name", googleName).apply();
    }

    /**
     * This function return whether the user prefer to use his google photo in multiplayer or not
     *
     * @return true if the user prefer to his google photo in multiplayer or not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    static boolean getGooglePhoto() {
        return sharedPreferences.getBoolean("google_photo", false);
    }

    /**
     * This function reads from the local file info depending on the {@param param}
     *
     * @param param the stat to read from the file
     * @return the stat from the file
     */
    static int readStat(@NotNull Readables param) {
        return sharedPreferences.getInt(param.toString(), 0);
    }

    /**
     * This function writes to the local file based on the {@param type} and the {@param message}
     *
     * @param type    the type to write to
     * @param message the message to write to the type
     */
    @SuppressLint("DefaultLocale")
    static private void writeFile(@NotNull Readables type, int message) {
        sharedPreferences.edit().putInt(type.toString(), message).apply();
    }

    /**
     * This function adds 1 X win to the shared preferences
     */
    @SuppressWarnings("ConstantConditions")
    static void addXwins() {
        writeFile(Readables.Xwins, readStat(Readables.Xwins) + 1);
        if (Firebase.userRef != null) {
            Firebase.userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Firebase.userRef.child("Xwins").setValue(snapshot.child("Xwins").getValue(Integer.class) + 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    /**
     * This function sets the local x wins to the {@param x}
     *
     * @param x the number of x wins to set
     */
    static void setXwins(int x) {
        writeFile(Readables.Xwins, x);
    }

    /**
     * This function adds 1 O win to the shared preferences
     */
    @SuppressWarnings("ConstantConditions")
    static void addOwins() {
        writeFile(Readables.Owins, readStat(Readables.Owins) + 1);
        if (Firebase.userRef != null) {
            Firebase.userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Firebase.userRef.child("Owins").setValue(snapshot.child("Owins").getValue(Integer.class) + 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    /**
     * This function sets the local x wins to the {@param o}
     *
     * @param o the number of x wins to set
     */
    static void setOwins(int o) {
        writeFile(Readables.Owins, o);
    }

    /**
     * This function adds {@param time} seconds to the shared preferences
     */
    static void addTime(long time) {
        writeFile(Readables.Time, readStat(Readables.Time) + (int)time);
    }

    /**
     * This function sets the local time in the shared preferences to {@param time}
     *
     * @param time the time to write to the shared preferences
     */
    static void setTime(int time) {
        sharedPreferences.edit().putInt("Time", time).apply();
    }

    /**
     * This is the possible readings from the local file
     */
    public enum Readables {
        Xwins, Owins, Time
    }
}

/**
 * This class represents the lobby
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@Keep
class Lobby {
    private String clientMessage;
    private String clientName;
    private String hostMessage;
    private String hostName;
    private String hostType;
    private boolean matchmaking;
    private int max;
    private boolean privacy;
    private String startingType;
    private int timer;

    public Lobby(String hostName, boolean matchmaking, int max, boolean privacy, String startingType, int timer) {
        this.hostName = hostName;
        this.hostType = "O";
        this.matchmaking = matchmaking;
        this.max = max;
        this.privacy = privacy;
        this.startingType = startingType;
        this.timer = timer;
    }

    public Lobby() {
    }

    public String getClientMessage() {
        return clientMessage;
    }

    public void setClientMessage(String clientMessage) {
        this.clientMessage = clientMessage;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getHostMessage() {
        return hostMessage;
    }

    public void setHostMessage(String hostMessage) {
        this.hostMessage = hostMessage;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    public boolean isMatchmaking() {
        return matchmaking;
    }

    public void setMatchmaking(boolean matchmaking) {
        this.matchmaking = matchmaking;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public boolean isPrivacy() {
        return privacy;
    }

    public void setPrivacy(boolean privacy) {
        this.privacy = privacy;
    }

    public String getStartingType() {
        return startingType;
    }

    public void setStartingType(String startingType) {
        this.startingType = startingType;
    }

    public int getTimer() {
        return timer;
    }

    public void setTimer(int timer) {
        this.timer = timer;
    }
}
