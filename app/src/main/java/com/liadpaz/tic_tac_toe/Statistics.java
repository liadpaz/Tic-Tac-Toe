package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.util.Objects;

public class Statistics extends AppCompatActivity {

    DatabaseReference statsRef;

    Button btn_return;

    TextView tv_localO;
    TextView tv_localX;
    TextView tv_globalO;
    TextView tv_globalX;
    TextView tv_localTime;
    TextView tv_globalTime;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_statistics));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        btn_return = findViewById(R.id.btn_return_stats);
        tv_localO = findViewById(R.id.tv_local_wins_O);
        tv_localX = findViewById(R.id.tv_local_wins_X);
        tv_globalO = findViewById(R.id.tv_global_wins_O);
        tv_globalX = findViewById(R.id.tv_global_wins_X);
        tv_localTime = findViewById(R.id.tv_local_time);
        tv_globalTime = findViewById(R.id.tv_global_time);

        tv_localO.setText(String.valueOf(Stats.readFile(Stats.Readables.Owins)));
        tv_localX.setText(String.valueOf(Stats.readFile(Stats.Readables.Xwins)));
        tv_localTime.setText(String.format("%s %s", String.valueOf(Stats.readFile(Stats.Readables.Time)), getString(R.string.Seconds)));

        new AsyncTask<Void, Void, Boolean>() {
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
                    getGlobals();
                } else {
                    tv_globalO.setText(getString(R.string.NotAvailableOffline));
                    tv_globalX.setText(getString(R.string.NotAvailableOffline));
                    tv_globalTime.setText(getString(R.string.NotAvailableOffline));
                }
                super.onPostExecute(aBoolean);
            }
        }.execute();
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_statistics, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(Statistics.this)
                .setTitle(R.string.ResetData)
                .setMessage(R.string.ResetDataDialog)
                .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Stats.resetFile();
                        tv_localO.setText("0");
                        tv_localX.setText("0");
                        tv_localTime.setText("0");
                        Utils.setTime();
                    }
                })
                .setNegativeButton(R.string.No, null)
                .setCancelable(true);
        alert.show();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function gets the global stats and writes them
     */
    void getGlobals() {

        tv_globalO.setText(R.string.Loading);
        tv_globalX.setText(R.string.Loading);
        tv_globalTime.setText(R.string.Loading);

        statsRef = Firebase.dataRef;

        statsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tv_globalO.setText(String.valueOf(dataSnapshot.child("Owins").getValue(Integer.class)));
                tv_globalX.setText(String.valueOf(dataSnapshot.child("Xwins").getValue(Integer.class)));
                tv_globalTime.setText(String.format("%d %s", dataSnapshot.child("Time").getValue(Integer.class), R.string.Seconds));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
