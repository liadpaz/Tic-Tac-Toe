package com.example.tic_tac_toe;

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

public class LobbyActivity extends Activity {

    DatabaseReference lobbyRef;

    Integer max;
    Integer timer;

    private Button btn_exit_lobby;

    private TextView tv_host_name;
    private TextView tv_client_name;
    private TextView tv_room_number;

    private Switch sw_host;
    private Switch sw_client;

    private CheckBox ckbx_ready;

    private String multiplayerMode;
    private String lobbyNumber;
    private String hostName;
    private String clientName;

    private ValueEventListener valueEventListener;

    private boolean ready_host = false;
    private boolean ready_client = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        lobbyNumber = getIntent().getStringExtra("LobbyNumber");

        multiplayerMode = getIntent().getStringExtra("Multiplayer");
        hostName = getIntent().getStringExtra("HostName");

        btn_exit_lobby = findViewById(R.id.btn_exit_lobby);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_client_name = findViewById(R.id.tv_client_name);
        tv_room_number = findViewById(R.id.tv_room_ip);
        sw_host = findViewById(R.id.sw_host_side);
        sw_client = findViewById(R.id.sw_client_side);
        ckbx_ready = findViewById(R.id.ckbx_ready);

        tv_room_number.setText(lobbyNumber);

        tv_host_name.setText(hostName);
        tv_client_name.setText(getString(R.string.Waiting));

        sw_client.setEnabled(false);
        sw_client.setChecked(true);

        if (multiplayerMode.equals("Host")) {
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
                if (multiplayerMode.equals("Host")) {
                    ready_host = isChecked;
                    sw_host.setEnabled(!isChecked);
                    if (ready_host && ready_client) {

                        writeDatabaseMessage("play");

                        startActivity(new Intent(LobbyActivity.this, Game.class)
                            .putExtra("Max", max)
                            .putExtra("Timer", timer)
                            .putExtra("lobbyNumber", lobbyNumber));

                        finish();

                    }
                } else {
                    if (isChecked)
                        writeDatabaseMessage("ready");
                    else
                        writeDatabaseMessage("not_ready");
                }
            }
        });
        btn_exit_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LobbyActivity.this.finish();
            }
        });

        if (multiplayerMode.equals("Host"))
            ckbx_ready.setEnabled(false);

        lobbyRef = Database.dataRef.child("Lobbies").child(lobbyNumber);

        valueEventListener = lobbyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (max == null || timer == null) {
                    max = dataSnapshot.child("max").getValue(Integer.class);
                    timer = dataSnapshot.child("timer").getValue(Integer.class);
                }

                if (multiplayerMode.equals("Host")) {

                    if (clientName == null) {

                        clientName = dataSnapshot.child("clientName").getValue(String.class);
                        if (clientName != null) {
                            tv_client_name.setText(clientName);
                            ckbx_ready.setEnabled(true);
                        }
                    }

                    String clientMessage = dataSnapshot.child("clientMessage").getValue(String.class);
                    if (clientMessage != null) {
                        switch (clientMessage) {
                            case "ready":
                                ready_client = true;
                                if (ready_host) {

                                    writeDatabaseMessage("play");

                                    try {
                                        startActivity(new Intent(LobbyActivity.this, Game.class)
                                                .putExtra("LobbyNumber", lobbyNumber)
                                                .putExtra("Multiplayer", "Host"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
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

                                                lobbyRef.setValue(null);

                                                finishAffinity();

                                                startActivity(new Intent(LobbyActivity.this, MainActivity.class));

                                            }
                                        })
                                        .setCancelable(false);
                                alert.show();
                                break;
                        }
                    }
                } else {

                    if (hostName == null) {

                        hostName = dataSnapshot.child("hostName").getValue(String.class);
                        clientName = dataSnapshot.child("clientName").getValue(String.class);
                        tv_host_name.setText(hostName);
                        tv_client_name.setText(clientName);

                    }

                    swapSwitches(dataSnapshot.child("hostType").getValue(String.class));

                    String hostMessage = dataSnapshot.child("hostMessage").getValue(String.class);
                    if (hostMessage != null) {

                        if (hostMessage.equals("play")) {

                            startActivity(new Intent(LobbyActivity.this, Game.class)
                                    .putExtra("LobbyNumber", lobbyNumber)
                                    .putExtra("Multiplayer", "Client"));

                            finish();
                        } else if (hostMessage.equals("left")) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(LobbyActivity.this)
                                    .setMessage(getString(R.string.LobbyLeftMessage))
                                    .setPositiveButton(getString(R.string.BackMainMenu), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            lobbyRef.setValue(null);

                                            finishAffinity();
                                            startActivity(new Intent(LobbyActivity.this, MainActivity.class));

                                        }
                                    })
                                    .setCancelable(false);
                            alert.show();
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void writeDatabaseMessage(String message) {
        if (multiplayerMode.equals("Host"))
            lobbyRef.child("hostMessage").setValue(message);
        else
            lobbyRef.child("clientMessage").setValue(message);
    }

    private void setHostType(boolean type) {
        lobbyRef.child("hostType").setValue(type ? "X" : "O");
    }

    private void swapSwitches(String hostType) {
        if (hostType.equals("X")) {
            sw_host.setChecked(true);
            sw_client.setChecked(false);
        } else {
            sw_host.setChecked(false);
            sw_client.setChecked(true);
        }
    }

    @Override
    protected void onStop() {

        lobbyRef.removeEventListener(valueEventListener);

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        lobbyRef.setValue(null);
        if (multiplayerMode.equals("Host") && clientName != null)
            writeDatabaseMessage("left");
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if ((multiplayerMode.equals("Host") && clientName != null) || multiplayerMode.equals("Client"))
            writeDatabaseMessage("left");
        super.onDestroy();
    }
}
