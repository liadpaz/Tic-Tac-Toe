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
import com.liadpaz.tic_tac_toe.databinding.ActivityStatisticsBinding;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.util.Objects;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tv_globalO;
    private TextView tv_globalX;
    private TextView tv_globalTime;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityStatisticsBinding binding = ActivityStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarStatistics);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.tvLocalWinsO.setText(String.valueOf(Stats.readStat(Stats.Readables.Owins)));
        binding.tvLocalWinsX.setText(String.valueOf(Stats.readStat(Stats.Readables.Xwins)));
        tv_globalO = binding.tvGlobalWinsO;
        tv_globalX = binding.tvGlobalWinsX;
        binding.tvLocalTime.setText(String.format("%s %s", String.valueOf(Stats.readStat(Stats.Readables.Time)), getString(R.string.seconds)));
        tv_globalTime = binding.tvGlobalTime;

        new Task(this).execute();
    }

    /**
     * This function gets the global stats and writes them
     */
    void getGlobals() {
        tv_globalO.setText(R.string.loading);
        tv_globalX.setText(R.string.loading);
        tv_globalTime.setText(R.string.loading);

        DatabaseReference statsRef = Firebase.dataRef.child("Users");

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressWarnings("ConstantConditions")
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long time = 0;
                int xWins = 0;
                int oWins = 0;
                for (DataSnapshot user : dataSnapshot.getChildren()) {
                    for (DataSnapshot info : user.getChildren()) {
                        if (Objects.equals(info.getKey(), "Time")) {
                            time += info.getValue(Integer.class);
                        } else if (Objects.equals(info.getKey(), "Xwins")) {
                            xWins += info.getValue(Integer.class);
                        } else {
                            oWins += info.getValue(Integer.class);
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

        private WeakReference<StatisticsActivity> activityReference;

        Task(StatisticsActivity activityReference) {
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
                    activityReference.get().tv_globalO.setText(R.string.authenticated_users);
                    activityReference.get().tv_globalX.setText(R.string.authenticated_users);
                    activityReference.get().tv_globalTime.setText(R.string.authenticated_users);
                }
            } else {
                activityReference.get().tv_globalO.setText(R.string.not_available_offline);
                activityReference.get().tv_globalX.setText(R.string.not_available_offline);
                activityReference.get().tv_globalTime.setText(R.string.not_available_offline);
            }
            super.onPostExecute(aBoolean);
        }
    }
}
