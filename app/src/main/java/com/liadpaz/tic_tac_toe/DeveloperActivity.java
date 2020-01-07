package com.liadpaz.tic_tac_toe;

import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DeveloperActivity extends AppCompatActivity {

    ArrayList<String> lobbiesNumber;

    ListView lv_lobbies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        setSupportActionBar(findViewById(R.id.toolbar_developer));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        lobbiesNumber = new ArrayList<>();

        lv_lobbies = findViewById(R.id.lv_lobbies);

        getLobbies();
    }

    private void getLobbies() {
        Firebase.dataRef.child("Lobbies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lobbiesNumber.clear();

                for (DataSnapshot lobby : dataSnapshot.getChildren()) {
                    lobbiesNumber.add(lobby.getKey());
                }

                final LobbiesAdapter adapter = new LobbiesAdapter(DeveloperActivity.this, lobbiesNumber);
                lv_lobbies.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
