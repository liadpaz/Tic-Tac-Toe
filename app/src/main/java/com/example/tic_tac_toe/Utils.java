package com.example.tic_tac_toe;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

class Utils {
    public enum Mode {TwoPlayer, Computer, Multiplayer}

    static long last_time;

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
        } catch (Exception ignored) {
        }
        return null;
    }
}

class Database implements ValueEventListener {

    private static String value;
    private static String key;

    public static String Value() {
        return value;
    }

//    public void writeLobbyDatabase(String type, String message) {
//        lobbyRef.child(type).setValue(message);
//    }
//
//    public void writeClientDatabase(String query, String type, String message) {
//        if (query.equals("Lobby"))
//            lobbyRef.child(type).setValue(message);
//        else if (query.equals("Games"))
//            gamesRef.child(type).setValue(message);
//    }

    static void readServer(String... children) throws Exception {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for (String child: children) {
            reference = reference.child(child);
        }
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                value = dataSnapshot.getValue(String.class);
                key   = dataSnapshot.getKey();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        reference.child("KeepAlive").setValue(1);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Toast.makeText(new JoinMultiplayer().getApplicationContext(), "Reading",Toast.LENGTH_LONG).show();
        value = dataSnapshot.getValue(String.class);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.w("", "loadInfo:onCancelled", databaseError.toException());
    }
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

    public Type getType() {
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
        Time
    }
    static File file;

    static void setFile(File parent, String child) {
        file = new File(parent, child);
        if (readFile(Readables.Xwins) == null)
            writeFileAll("0");
    }

    static String readFile(@NonNull Readables param) {
        StringBuffer stringBuffer = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while (line != null) {
                stringBuffer.append(line).append('\n');
                line = reader.readLine();
            }
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

    static void writeFile(Readables type, String message) {
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

    static void writeFileAll(String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(String.format("%s %s %s", message, message, message));
            writer.close();
        } catch (Exception ignored) {
        }
    }
}