package com.example.tic_tac_toe;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.internal.Objects;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DeveloperActivity extends AppCompatActivity {

    DatabaseReference devRef;

    ArrayList<String> lobbiesNumber;

    ListView lv_lobbies;

    EditText et_dev_name;
    EditText et_dev_password;
    EditText et_lobby;

    Button btn_check_dev;
    Button btn_delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);
        this.setTitle("Developer");

        lobbiesNumber = new ArrayList<>();

        lv_lobbies = findViewById(R.id.lv_lobbies);
        et_dev_name = findViewById(R.id.et_dev_name);
        et_dev_password = findViewById(R.id.et_dev_password);
        et_lobby = findViewById(R.id.et_lobby);
        btn_check_dev = findViewById(R.id.btn_check_dev);
        btn_delete = findViewById(R.id.btn_delete);

        btn_check_dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devRef = Database.dataRef.child("DevUser");

                devRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("Name").getValue(String.class).equals(et_dev_name.getText().toString()) && dataSnapshot.child("Password").getValue(String.class).equals(et_dev_password.getText().toString())) {

                            getLobbies();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
        btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Database.dataRef.child("Lobbies").child(et_lobby.getText().toString()).setValue(null);
                getLobbies();
            }
        });
    }

    private void getLobbies() {

        Database.dataRef.child("Lobbies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                lobbiesNumber.clear();

                for (DataSnapshot lobby : dataSnapshot.getChildren()) {
                    lobbiesNumber.add(lobby.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(DeveloperActivity.this, R.layout.support_simple_spinner_dropdown_item, lobbiesNumber);

                lv_lobbies.setAdapter(adapter);

                et_dev_name.setVisibility(View.INVISIBLE);
                et_dev_password.setVisibility(View.INVISIBLE);
                btn_check_dev.setVisibility(View.INVISIBLE);
                lv_lobbies.setVisibility(View.VISIBLE);
                btn_delete.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
