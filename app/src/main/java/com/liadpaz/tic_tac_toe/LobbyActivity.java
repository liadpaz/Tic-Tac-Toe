package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class LobbyActivity extends AppCompatActivity {

    DatabaseReference lobbyRef;
    StorageReference storageRef;

    Integer max;
    Integer timer;

    boolean isHost;

    TextView tv_host_name;
    TextView tv_client_name;

    Switch sw_host;
    Switch sw_client;

    CheckBox ckbx_ready;

    String lobbyNumber;
    String hostName;
    String clientName;

    ValueEventListener listener;

    boolean ready_host = false;
    boolean ready_client = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobbyNumber = getIntent().getStringExtra("LobbyNumber");
        isHost = Objects.equals(getIntent().getStringExtra("Multiplayer"), "Host");
        hostName = getIntent().getStringExtra("HostName");

        Button btn_exit_lobby = findViewById(R.id.btn_exit_lobby);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_client_name = findViewById(R.id.tv_client_name);
        TextView tv_room_number = findViewById(R.id.tv_room_number);
        sw_host = findViewById(R.id.sw_host_side);
        sw_client = findViewById(R.id.sw_client_side);
        ckbx_ready = findViewById(R.id.ckbx_ready);

        tv_room_number.setText(lobbyNumber);

        tv_host_name.setText(hostName);
        tv_client_name.setText(R.string.Waiting);

        sw_client.setEnabled(false);
        sw_client.setChecked(true);

        if (isHost) {
            sw_host.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sw_client.setChecked(!isChecked);
                LobbyActivity.this.setHostType(isChecked);
            });
        } else {
            sw_host.setEnabled(false);
        }

        ckbx_ready.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isHost) {
                ready_host = isChecked;
                sw_host.setEnabled(!isChecked);
                if (ready_host && ready_client) {
                    LobbyActivity.this.writeDatabaseMessage("play");
                    LobbyActivity.this.startActivity(new Intent(LobbyActivity.this, Game.class)
                            .putExtra("LobbyNumber", lobbyNumber)
                            .putExtra("Multiplayer", "Host"));

                }
            } else {
                LobbyActivity.this.writeDatabaseMessage(isChecked ? "ready" : "not_ready");
            }
        });
        btn_exit_lobby.setOnClickListener(v -> {
            if (isHost) {
                if (clientName != null) {
                    writeDatabaseMessage("left");
                }
            } else {
                writeDatabaseMessage("left");
            }
            finish();
        });

        ckbx_ready.setEnabled(!isHost);

        lobbyRef = Firebase.dataRef.child("Lobbies").child(lobbyNumber);

        listener = lobbyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (max == null || timer == null) {
                    max = dataSnapshot.child("max").getValue(Integer.class);
                    timer = dataSnapshot.child("timer").getValue(Integer.class);
                }
                if (isHost) {
                    if (clientName == null) {
                        clientName = dataSnapshot.child("clientName").getValue(String.class);
                        if (clientName != null) {
                            tv_client_name.setText(clientName);
                            ckbx_ready.setEnabled(true);
                        }
                    } else {
                        String clientMessage = dataSnapshot.child("clientMessage").getValue(String.class);
                        if (clientMessage != null) {
                            switch (clientMessage) {
                                case "ready": {
                                    ready_client = true;
                                    if (ready_host) {
                                        writeDatabaseMessage("play");
                                        startActivity(new Intent(LobbyActivity.this, Game.class)
                                                .putExtra("LobbyNumber", lobbyNumber)
                                                .putExtra("Multiplayer", "Host"));
                                        finish();
                                    }
                                    break;
                                }

                                case "not_ready": {
                                    ready_client = false;
                                    break;
                                }

                                case "left": {
                                    new AlertDialog.Builder(LobbyActivity.this)
                                            .setMessage(R.string.LobbyLeftMessage)
                                            .setPositiveButton(R.string.BackMainMenu, (dialog, which) -> {
                                                lobbyRef.removeValue();
                                                finishAffinity();
                                                startActivity(new Intent(LobbyActivity.this, MainActivity.class));
                                            })
                                            .setCancelable(false)
                                            .show();
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    if (hostName == null) {
                        hostName = dataSnapshot.child("hostName").getValue(String.class);
                        clientName = dataSnapshot.child("clientName").getValue(String.class);
                        tv_host_name.setText(hostName);
                        tv_client_name.setText(clientName);
                    }

                    String hostType;
                    if ((hostType = dataSnapshot.child("hostType").getValue(String.class)) != null) {
                        swapSwitches(hostType);
                    }

                    String hostMessage = dataSnapshot.child("hostMessage").getValue(String.class);
                    if (hostMessage != null) {
                        switch (hostMessage) {
                            case "play": {
                                startActivity(new Intent(LobbyActivity.this, Game.class)
                                        .putExtra("LobbyNumber", lobbyNumber)
                                        .putExtra("Multiplayer", "Client"));
                                finish();
                                break;
                            }

                            case "left": {
                                new AlertDialog.Builder(LobbyActivity.this)
                                        .setMessage(R.string.LobbyLeftMessage)
                                        .setPositiveButton(R.string.BackMainMenu, (dialog, which) -> {
                                            lobbyRef.removeValue();
                                            finishAffinity();
                                            startActivity(new Intent(LobbyActivity.this, MainActivity.class));
                                        })
                                        .setCancelable(false)
                                        .show();
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        if (!Stats.readPrivacy()) {
            uploadPhoto();
        }
    }

    /**
     * This function writes a message to the database based on the player type
     *
     * @param message the message to write
     */
    private void writeDatabaseMessage(String message) {
        if (isHost) {
            lobbyRef.child("hostMessage").setValue(message);
        } else {
            lobbyRef.child("clientMessage").setValue(message);
        }
    }

    /**
     * This function writes to the database the host type
     *
     * @param type true if the host is X, false if the host is O
     */
    private void setHostType(boolean type) {
        lobbyRef.child("hostType").setValue(type ? "X" : "O");
    }

    /**
     * This function swap the types switches graphically
     *
     * @param hostType the type of the host
     */
    private void swapSwitches(String hostType) {
        if (hostType.equals("X")) {
            sw_host.setChecked(true);
            sw_client.setChecked(false);
        } else {
            sw_host.setChecked(false);
            sw_client.setChecked(true);
        }
    }

    /**
     * This function uploads the photo the player took to the Firebase Storage
     */
    private void uploadPhoto() {
        storageRef = Firebase.storeRef.child("Lobbies").child(lobbyNumber);
        storageRef.child(isHost ? "Host" : "Client").putFile(Utils.localPhotoUri);
    }

    @Override
    protected void onDestroy() {
        lobbyRef.removeEventListener(listener);
        if (isHost && clientName == null) {
            lobbyRef.removeValue();
        }
        super.onDestroy();
    }
}
