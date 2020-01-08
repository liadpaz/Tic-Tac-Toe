package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Objects;

public class Statistics extends AppCompatActivity {

    DatabaseReference statsRef;

    TextView tv_localO;
    TextView tv_localX;
    TextView tv_globalO;
    TextView tv_globalX;
    TextView tv_localTime;
    TextView tv_globalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setSupportActionBar(findViewById(R.id.toolbar_statistics));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        tv_localO = findViewById(R.id.tv_local_wins_O);
        tv_localX = findViewById(R.id.tv_local_wins_X);
        tv_globalO = findViewById(R.id.tv_global_wins_O);
        tv_globalX = findViewById(R.id.tv_global_wins_X);
        tv_localTime = findViewById(R.id.tv_local_time);
        tv_globalTime = findViewById(R.id.tv_global_time);

        tv_localO.setText(String.valueOf(Stats.readStat(Stats.Readables.Owins)));
        tv_localX.setText(String.valueOf(Stats.readStat(Stats.Readables.Xwins)));
        tv_localTime.setText(String.format("%s %s", String.valueOf(Stats.readStat(Stats.Readables.Time)), getString(R.string.seconds)));

        new Task(this).execute();
    }

    /**
     * This function gets the global stats and writes them
     */
    void getGlobals() {
        tv_globalO.setText(R.string.loading);
        tv_globalX.setText(R.string.loading);
        tv_globalTime.setText(R.string.loading);

        statsRef = Firebase.dataRef.child("Users");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long time = 0;
                int xWins = 0;
                int oWins = 0;
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    for (DataSnapshot info : user.getChildren()) {
                        if (Objects.equals(info.getKey(), "Time")) {
                            time += Objects.requireNonNull(info.getValue(Integer.class));
                        } else if (Objects.equals(info.getKey(), "Xwins")) {
                            xWins += Objects.requireNonNull(info.getValue(Integer.class));
                        } else {
                            oWins += Objects.requireNonNull(info.getValue(Integer.class));
                        }
                    }
                }
                tv_globalO.setText(String.valueOf(oWins));
                tv_globalX.setText(String.valueOf(xWins));
                tv_globalTime.setText(String.format("%d %s", time, getString(R.string.seconds)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private static class Task extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Statistics> activityReference;

        Task(Statistics activityReference) {
            this.activityReference = new WeakReference<>(activityReference);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                return InetAddress.getByName("www.google.com").isReachable(2000);
            } catch (Exception ignored) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    activityReference.get().getGlobals();
                } else {
                    activityReference.get().tv_globalO.setText(activityReference.get().getString(R.string.authenticated_users));
                    activityReference.get().tv_globalX.setText(activityReference.get().getString(R.string.authenticated_users));
                    activityReference.get().tv_globalTime.setText(activityReference.get().getString(R.string.authenticated_users));
                }
            } else {
                activityReference.get().tv_globalO.setText(activityReference.get().getString(R.string.not_available_offline));
                activityReference.get().tv_globalX.setText(activityReference.get().getString(R.string.not_available_offline));
                activityReference.get().tv_globalTime.setText(activityReference.get().getString(R.string.not_available_offline));
            }
            super.onPostExecute(aBoolean);
        }
    }
}
