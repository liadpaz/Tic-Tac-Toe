package com.liadpaz.tic_tac_toe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class LobbyActivity extends Activity {

    DatabaseReference lobbyRef;
    StorageReference storageRef;

    private Integer max;
    private Integer timer;

    private boolean isHost;
    private boolean isClient;

    private TextView tv_host_name;
    private TextView tv_client_name;

    private Switch sw_host;
    private Switch sw_client;

    private CheckBox ckbx_ready;

    private String lobbyNumber;
    private String hostName;
    private String clientName;

    private ValueEventListener templateValueListener;
    private ValueEventListener valueListener;

    private boolean ready_host = false;
    private boolean ready_client = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobbyNumber = getIntent().getStringExtra("LobbyNumber");
        isHost = Objects.equals(getIntent().getStringExtra("Multiplayer"), "Host");
        isClient = !isHost;
        hostName = getIntent().getStringExtra("HostName");

        Button btn_exit_lobby = findViewById(R.id.btn_exit_lobby);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_client_name = findViewById(R.id.tv_client_name);
        TextView tv_room_number = findViewById(R.id.tv_room_ip);
        sw_host = findViewById(R.id.sw_host_side);
        sw_client = findViewById(R.id.sw_client_side);
        ckbx_ready = findViewById(R.id.ckbx_ready);

        tv_room_number.setText(lobbyNumber);

        tv_host_name.setText(hostName);
        tv_client_name.setText(getString(R.string.Waiting));

        sw_client.setEnabled(false);
        sw_client.setChecked(true);

        if (isHost) {
            sw_host.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    sw_client.setChecked(!isChecked);
                    setHostType(isChecked);
                }
            });
        } else {
            sw_host.setEnabled(false);
        }

        ckbx_ready.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isHost) {
                    ready_host = isChecked;
                    sw_host.setEnabled(!isChecked);
                    if (ready_host && ready_client) {
                        writeDatabaseMessage("play");
                        startActivity(new Intent(LobbyActivity.this, Game.class)
                            .putExtra("lobbyNumber", lobbyNumber)
                            .putExtra("Multiplayer", "Host"));

                    }
                } else {
                    if (isChecked) {
                        writeDatabaseMessage("ready");
                    } else {
                        writeDatabaseMessage("not_ready");
                    }
                }
            }
        });
        btn_exit_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHost) {
                    if (clientName == null) {
                        lobbyRef.removeValue();
                    } else {
                        writeDatabaseMessage("left");
                    }
                } else {
                    writeDatabaseMessage("left");
                }
                LobbyActivity.this.finish();
            }
        });

        ckbx_ready.setEnabled(!isHost);

        lobbyRef = Firebase.dataRef.child("Lobbies").child(lobbyNumber);

        templateValueListener = new ValueEventListener() {
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
                                case "ready":
                                    ready_client = true;
                                    if (ready_host) {
                                        writeDatabaseMessage("play");
                                        startActivity(new Intent(LobbyActivity.this, Game.class)
                                                .putExtra("LobbyNumber", lobbyNumber)
                                                .putExtra("Multiplayer", "Host"));
                                    }
                                    break;

                                case "not_ready":
                                    ready_client = false;
                                    break;

                                case "left":
                                    AlertDialog.Builder alert = new AlertDialog.Builder(LobbyActivity.this)
                                            .setMessage(getString(R.string.LobbyLeftMessage))
                                            .setPositiveButton(getString(R.string.BackMainMenu), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    lobbyRef.removeValue();
                                                    startActivity(new Intent(LobbyActivity.this, MainActivity.class));
                                                }
                                            })
                                            .setCancelable(false);
                                    alert.show();
                                    break;
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

                    swapSwitches(Objects.requireNonNull(dataSnapshot.child("hostType").getValue(String.class)));

                    String hostMessage = dataSnapshot.child("hostMessage").getValue(String.class);
                    if (hostMessage != null) {
                        switch (hostMessage) {

                            case "play":

                                startActivity(new Intent(LobbyActivity.this, Game.class)
                                        .putExtra("LobbyNumber", lobbyNumber)
                                        .putExtra("Multiplayer", "Client"));

                                break;

                            case "left":
                                AlertDialog.Builder alert = new AlertDialog.Builder(LobbyActivity.this)
                                        .setMessage(getString(R.string.LobbyLeftMessage))
                                        .setPositiveButton(getString(R.string.BackMainMenu), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                lobbyRef.removeValue();

                                                startActivity(new Intent(LobbyActivity.this, MainActivity.class));

                                            }
                                        })
                                        .setCancelable(false);
                                alert.show();

                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        if (!Stats.readPrivacy()) {
            uploadPhoto();
        }
    }

    @Override
    protected void onResume() {
        valueListener = lobbyRef.addValueEventListener(templateValueListener);
        super.onResume();
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

    private void uploadPhoto() {
        storageRef = Firebase.storeRef.child("Lobbies").child(lobbyNumber);
        if (isHost) {
            storageRef.child("Host").putFile(Utils.localPhotoUri);
        } else {
            storageRef.child("Client").putFile(Utils.localPhotoUri);
        }
    }

    @Override
    protected void onPause() {
        lobbyRef.removeEventListener(valueListener);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if ((isHost && clientName != null) || isClient) {
            writeDatabaseMessage("left");
        } else {
            lobbyRef.removeValue();
        }
        super.onDestroy();
    }
}
