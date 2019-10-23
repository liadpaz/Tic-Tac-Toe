package com.example.tic_tac_toe;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Random;

class Utils {
    public enum Mode {TwoPlayer, Computer, Multiplayer}

    private static long last_time;

    static void setTime() {
        last_time = Calendar.getInstance().getTime().getTime();
    }

    static long getTime() {
        long time = (Calendar.getInstance().getTime().getTime() - last_time) / 1000;
        setTime();
        return time;
    }


    static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%');
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }
}

class Database {

    static DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference();

    private static List<String> message = new ArrayList<>();
    private static String Ostat = null;
    private static String Xstat = null;
    private static String timeStat = null;
}

class Cell {
    public enum Type {
        X,
        O
    }

    private ImageView[] XO;
    private Type type;
    private boolean visible = false;

    Cell(ImageView[] XO) {
        this.XO = XO;
    }

    boolean isVisible() {
        return visible;
    }

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

    Type getType() {
        return type;
    }

    void setLocation(int x, int y) {
        for (ImageView iv : XO) {
            iv.setX(x);
            iv.setY(y);
        }
    }

    void setSize(int x) {
        for (ImageView iv : XO) {
            iv.setLayoutParams(new ConstraintLayout.LayoutParams(x, x));
        }
    }

    void hide() {
        visible = false;
        type = null;
        XO[0].setVisibility(View.GONE);
        XO[1].setVisibility(View.GONE);
    }
}

class Player {
    public enum Type {
        Human,
        CPU
    }

    final Type playerType;
    private int wins = 0;
    private final Cell.Type XO;

    Player(Type player, Cell.Type type) {
        playerType = player;
        this.XO = type;
    }

    void won() {
        wins++;
    }

    public Cell.Type getXO() {
        return XO;
    }

    String getStringType() {
        if (playerType == Type.CPU) return "Computer";
        return "You";
    }

    String getWins() {
        return String.valueOf(wins);
    }

    int[] getRandom() {
        Random rnd = new Random();
        return new int[] {rnd.nextInt(3), rnd.nextInt(3)};
    }
}

class Stats {
    public enum Readables {
        Xwins,
        Owins,
        Time;

        String getRead() {
            if (this == Xwins)  return "Xwins";
            if (this == Owins)  return "Owins";
                                return "Time";
        }
    }
    private static File file;

    static void setFile(File parent) {
        file = new File(parent, "tic-tac-toe");
        if (readFile(Readables.Xwins) == null)
            writeFileAll("0");
    }

    static String readFile(Readables param) {
        StringBuffer stringBuffer = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            stringBuffer.append(line);
            String[] contents = stringBuffer.toString().split(" ");
            switch (param) {
                case Xwins:
                    return contents[0];

                case Owins:
                    return contents[1];

                case Time:
                    return contents[2];

                default:
                    return null;
            }
        } catch (IOException error) {
            return null;
        }
    }

    static private void writeFile(Readables type, String message) {
        try {
            String Xwins = type == Readables.Xwins ? message : readFile(Readables.Xwins);
            String Owins = type == Readables.Owins ? message : readFile(Readables.Owins);
            String Time  = type == Readables.Time ? message : readFile(Readables.Time);

            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%s %s %s", Xwins, Owins, Time));
            writer.close();
        } catch (Exception ignored) {
        }
    }

    static void addXwins() {
        writeFile(Readables.Xwins, String.valueOf(Integer.parseInt(readFile(Readables.Xwins)) + 1));
    }

    static void addOwins() {
        writeFile(Readables.Owins, String.valueOf(Integer.parseInt(readFile(Readables.Owins)) + 1));
    }

    static void addTime(long time) {
        writeFile(Readables.Time, String.valueOf(Integer.parseInt(readFile(Readables.Time)) + (int) time));
    }

    static void writeFileAll(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%s %s %s", message, message, message));
            writer.close();
        } catch (Exception ignored) {
        }
    }
}

class Lobby {
    String hostName;
    String clientName;
    String number;
    String message = null;
    boolean isFull = false;

    public Lobby() {}
    public Lobby(String number) {
        this.number = number;
    }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean getFull() { return isFull; }
    public void setFull(boolean isFull) { this.isFull = isFull; }
}

