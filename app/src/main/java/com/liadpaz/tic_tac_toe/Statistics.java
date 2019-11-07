package com.liadpaz.tic_tac_toe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class Statistics extends AppCompatActivity {

    DatabaseReference statsRef;

    Button btn_return;
    Button btn_reset;

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

        btn_return = findViewById(R.id.btn_return_stats);
        btn_reset = findViewById(R.id.btn_reset_stats);
        tv_localO = findViewById(R.id.tv_local_wins_O);
        tv_localX = findViewById(R.id.tv_local_wins_X);
        tv_globalO = findViewById(R.id.tv_global_wins_O);
        tv_globalX = findViewById(R.id.tv_global_wins_X);
        tv_localTime = findViewById(R.id.tv_local_time);
        tv_globalTime = findViewById(R.id.tv_global_time);

        tv_localO.setText(String.valueOf(Stats.readFile(Stats.Readables.Owins)));
        tv_localX.setText(String.valueOf(Stats.readFile(Stats.Readables.Xwins)));
        tv_localTime.setText(String.format("%s %s", String.valueOf(Stats.readFile(Stats.Readables.Time)), getString(R.string.Seconds)));

        if (Utils.isConnected()) {
            getGlobals();
        } else {
            tv_globalO.setText(getString(R.string.NotAvailableOffline));
            tv_globalX.setText(getString(R.string.NotAvailableOffline));
            tv_globalTime.setText(getString(R.string.NotAvailableOffline));
        }

        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(Statistics.this)
                        .setTitle(getString(R.string.ResetData))
                        .setMessage(getString(R.string.ResetDataDialog))
                        .setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Stats.resetFile();
                                tv_localO.setText("0");
                                tv_localX.setText("0");
                                tv_localTime.setText("0");
                                Utils.setTime();
                            }
                        })
                        .setNegativeButton(getString(R.string.No), null)
                        .setCancelable(true);
                alert.show();
            }
        });
    }

    /**
     * This function gets the global stats and writes them
     */
    void getGlobals() {

        tv_globalO.setText(getString(R.string.Loading));
        tv_globalX.setText(getString(R.string.Loading));
        tv_globalTime.setText(getString(R.string.Loading));

        statsRef = Database.dataRef;

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_globalO.setText(String.valueOf(dataSnapshot.child("Owins").getValue(Integer.class)));
                tv_globalX.setText(String.valueOf(dataSnapshot.child("Xwins").getValue(Integer.class)));
                tv_globalTime.setText(String.format("%d %s", dataSnapshot.child("Time").getValue(Integer.class), getString(R.string.Seconds)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
