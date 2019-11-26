package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Keep;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

/**
 * This class is for general utilities
 */
class Utils {
    /**
     * This enum is to differ a 'TwoPlayer', 'Computer' or Multiplayer gamemode
     */
    public enum Mode {TwoPlayer, Computer, Multiplayer}

    static Uri localPhotoUri;
    static Uri remotePhotoUri;

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
    static String getRoomNumber() {
        StringBuilder number = new StringBuilder();
        for (int i = 0; i < 4; i++)
            number.append((new Random()).nextInt(10));
        return number.toString();
    }

    /**
     * This function generates an array with a random location on the game board
     *
     * @return an array in size 2, containing a random location on the game board
     */
    static int[] getRandom() {
        Random rnd = new Random();
        return new int[] {rnd.nextInt(3), rnd.nextInt(3)};
    }

//    static int[] getSmartTurn(Cell[][] cells) {
//        return null;
//    }
}

/**
 * This class contains the main reference to the Firebase Firebase and Firebase Storage
 */
class Firebase {
    /**
     * The main reference to the Firebase Database
     */
    static DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();
    /**
     * The main reference to the Firebase Storage
     */
    static StorageReference storeRef = FirebaseStorage.getInstance().getReference();
}

/**
 * This class represents the game cell
 */
class Cell {
    /**
     * This is the type of the cell. eg. X, O
     */
    public enum Type {
        X,
        O;

        /**
         * This function flips the type. eg. X to O and O to X
         *
         * @return X if the type was O, and O if the type was X
         */
        Type flip() {
            if (this == X)
                return O;
            return X;
        }
    }

    private ImageView[] XO;
    private Type type;
    private boolean visible = false;

    /**
     * Constructor, attaches 2 ImageViews (X, O) to the cell
     *
     * @param XO an array of 2 ImageVies (X, O)
     */
    Cell(ImageView[] XO) {
        this.XO = XO;
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
     *
     * @return true if the function have done the transformation, otherwise false
     */
    boolean setType(Type type) {
        if (!visible) {
            this.type = type;
            if (type == Type.X)
                XO[0].setVisibility(View.VISIBLE);
            else /*if (type == Type.O)*/
                XO[1].setVisibility(View.VISIBLE);
            visible = true;
            return true;
        }
        return false;
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
        for (ImageView iv : XO) {
            iv.setX(x);
            iv.setY(y);
        }
    }

    /**
     * This function sets the cell size
     *
     * @param x the height and width of the cell
     */
    void setSize(int x) {
        for (ImageView iv : XO) {
            iv.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));
        }
    }

    /**
     * This function hides the cell from the player/s and sets his type to null
     */
    void hide() {
        visible = false;
        type = null;
        XO[0].setVisibility(View.GONE);
        XO[1].setVisibility(View.GONE);
    }
}

/**
 * This class represents the player
 */
class Player {
    /**
     * The player type. eg. 'CPU', 'Human'
     */
    public enum Type {
        Human,
        CPU
    }

    final Type playerType;
    private int wins = 0;

    /**
     * Constructor, sets the player type to the {@code player} type
     *
     * @param player the player type
     */
    Player(Type player) {
        playerType = player;
    }

    /**
     * This function adds 1 win to the player
     */
    void won() {
        wins++;
    }

    /**
     * This function returns the current wins of the player
     *
     * @return current wins of the player
     */
    int getWins() {
        return wins;
    }
}

/**
 * This class is for the statistics
 */
class Stats {
    /**
     * This is the possible readings from the local file
     */
    public enum Readables {
        Xwins,
        Owins,
        Time
    }

    private static File file;

    /**
     * This function sets the local file if it's already exists or not
     *
     * @param parent the directory
     */
    static void setFile(File parent) {
        file = new File(parent, "tic-tac-toe");
        try {
            if (readFile(Readables.Xwins) == -1) {
                resetFile();
            }
        } catch (Exception ignored) {
            resetFile();
        }
    }

    static boolean flipPrivacy() {
        boolean privacy = readPrivacy();
        writePrivacy(!privacy);
        return !privacy;
    }

    static boolean readPrivacy() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return Boolean.parseBoolean(reader.readLine().split(" ")[3]);
        } catch (Exception ignored) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    private static void writePrivacy(boolean privacy) {
        try {
            int Xwins = readFile(Readables.Xwins);
            int Owins = readFile(Readables.Owins);
            int Time = readFile(Readables.Time);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%d %d %d %s", Xwins, Owins, Time, String.valueOf(privacy)));
            writer.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * This function reads from the local file info depending on the {@param param}
     *
     * @param param the stat to read from the file
     *
     * @return the stat from the file
     */
    static int readFile(Readables param) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String[] contents = reader.readLine().split(" ");
            switch (param) {
                case Xwins:
                    return Integer.parseInt(contents[0]);

                case Owins:
                    return Integer.parseInt(contents[1]);

                case Time:
                    return Integer.parseInt(contents[2]);

                default:
                    return 0;
            }
        } catch (IOException error) {
            return -1;
        }
    }

    /**
     * This function writes to the local file based on the {@param type} and the {@param message}
     * @param type      the type to write to
     * @param message   the message to write to the type
     */
    @SuppressLint("DefaultLocale")
    static private void writeFile(Readables type, int message) {
        try {
            int Xwins = type == Readables.Xwins ? message : readFile(Readables.Xwins);
            int Owins = type == Readables.Owins ? message : readFile(Readables.Owins);
            int Time  = type == Readables.Time ? message : readFile(Readables.Time);
            boolean privacy = readPrivacy();

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%d %d %d %s", Xwins, Owins, Time, String.valueOf(privacy)));
            writer.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * This function adds 1 X win to the local file
     */
    static void addXwins() {
        writeFile(Readables.Xwins, readFile(Readables.Xwins) + 1);
    }

    /**
     * This function adds 1 O win to the local file
     */
    static void addOwins() {
        writeFile(Readables.Owins, readFile(Readables.Owins) + 1);
    }

    /**
     * This function adds {@param time} seconds to the local file
     */
    static void addTime(long time) {
        writeFile(Readables.Time, readFile(Readables.Time) + (int) time);
    }

    /**
     * This function resets the local file
     */
    @SuppressLint("DefaultLocale")
    static void resetFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%d %d %d %s", 0, 0, 0, String.valueOf(false)));
            writer.close();
        } catch (Exception ignored) {
        }
    }
}

/**
 * This class represents the lobby
 */
@Keep
class Lobby {
    public int max;
    public int timer;
    public boolean privacy;
    public String hostName;
    public String number;
    public String hostType;
    public String startingType;

    Lobby(String hostName, String number, String startingType, int timer, int max, boolean privacy) {
        this.hostName = hostName;
        this.number = number;
        this.startingType = startingType;
        this.timer = timer;
        this.max = max;
        this.privacy = privacy;
        this.hostType = "O";
    }
}
