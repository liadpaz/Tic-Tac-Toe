package com.example.tic_tac_toe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class JoinMultiplayer extends Activity {

    DatabaseReference joinRef;

    Button btn_return_join;
    Button btn_join;

    EditText et_name_join;
    EditText et_lobby_number;

    String clientName;
    String lobbyNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_multiplayer);

        joinRef = Database.dataRef.child("Lobbies");

        btn_return_join = findViewById(R.id.btn_return_join);
        btn_join = findViewById(R.id.btn_join);
        et_name_join = findViewById(R.id.et_name_join);
        et_lobby_number = findViewById(R.id.et_lobby_number);

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clientName = et_name_join.getText().toString();
                lobbyNumber = et_lobby_number.getText().toString();

                if (lobbyNumber.length() != "0000".length()) {
                    Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyLength), Toast.LENGTH_LONG).show();
                } else {
                    joinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (Objects.equals(data.getKey(), lobbyNumber)) {

                                    if (data.child("clientName").getValue(String.class) == null) {

                                        startActivity(new Intent(JoinMultiplayer.this, LobbyActivity.class)
                                                .putExtra("ClientName", clientName)
                                                .putExtra("Multiplayer", "Client")
                                                .putExtra("LobbyNumber", lobbyNumber));
                                        joinRef.child(lobbyNumber).child("clientName").setValue(clientName);

                                    } else {
                                        Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyFull), Toast.LENGTH_LONG).show();
                                    }
                                    return;
                                }
                            }
                            Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyNotFound), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
        btn_return_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_name_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_join.setEnabled(s.length() > 0 && et_lobby_number.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_lobby_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btn_join.setEnabled(s.length() > 0 && et_name_join.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_join.setEnabled(false);
    }
}


