package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.liadpaz.tic_tac_toe.Constants.User;
import com.liadpaz.tic_tac_toe.databinding.ActivityStatisticsBinding;

import java.net.InetAddress;

public class StatisticsActivity extends AppCompatActivity {

    private TextView tv_globalO;
    private TextView tv_globalX;
    private TextView tv_globalTime;

    @SuppressWarnings({"ConstantConditions"})
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

        new Thread(() -> {
            try {
                if (InetAddress.getByName("www.google.com").isReachable(2000)) {
                    runOnUiThread(() -> {
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            getGlobals();
                        } else {
                            tv_globalO.setText(R.string.authenticated_users);
                            tv_globalX.setText(R.string.authenticated_users);
                            tv_globalTime.setText(R.string.authenticated_users);
                        }
                    });
                }
            } catch (Exception ignored) {
                tv_globalO.setText(R.string.not_available_offline);
                tv_globalX.setText(R.string.not_available_offline);
                tv_globalTime.setText(R.string.not_available_offline);
            }
        }).start();
    }

    /**
     * This function gets the global stats and puts them on the proper text views
     */
    void getGlobals() {
        tv_globalO.setText(R.string.loading);
        tv_globalX.setText(R.string.loading);
        tv_globalTime.setText(R.string.loading);

        Firebase.dataRef.child(User.USERS).addValueEventListener(new ValueEventListener() {
            @SuppressWarnings("ConstantConditions")
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long time = 0;
                int xWins = 0;
                int oWins = 0;
                for (DataSnapshot user : snapshot.getChildren()) {
                    time += user.child(User.TIME).getValue(Integer.class);
                    xWins += user.child(User.X_WINS).getValue(Integer.class);
                    oWins += user.child(User.O_WINS).getValue(Integer.class);
                }
                tv_globalO.setText(String.valueOf(oWins));
                tv_globalX.setText(String.valueOf(xWins));
                tv_globalTime.setText(String.format("%d %s", time, getString(R.string.seconds)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
