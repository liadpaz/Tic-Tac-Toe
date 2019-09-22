package com.example.tic_tac_toe;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import java.net.Socket;


public class LobbyActivity extends AppCompatActivity implements View.OnClickListener {

    Socket host_socket;
    Socket client_socket;

    Button btn_exit_lobby;

    TextView tv_host_name;
    TextView tv_client_name;
    TextView tv_room_ip;

    Switch sw_host;
    Switch sw_client;

    CheckBox ckbx_ready;

    String multiplayer_mode = "";
    String server_address;
    String host_name;
    String client_name = null;

    boolean ready_host = false, ready_client = false;
    boolean type_host = false, type_client = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        Intent intent = getIntent();

        multiplayer_mode    = intent.getStringExtra("Multiplayer");
        host_name           = intent.getCharSequenceExtra("HostName").toString();

        btn_exit_lobby  = findViewById(R.id.btn_exit_lobby);
        tv_host_name    = findViewById(R.id.tv_host_name);
        tv_client_name  = findViewById(R.id.tv_client_name);
        tv_room_ip      = findViewById(R.id.tv_room_ip);
        sw_host         = findViewById(R.id.sw_host_side);
        sw_client       = findViewById(R.id.sw_client_side);
        ckbx_ready      = findViewById(R.id.ckbx_ready);

        btn_exit_lobby.setOnClickListener(this);
        tv_host_name.setText(host_name);
        tv_client_name.setText("Waiting...");
        ckbx_ready.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (multiplayer_mode.equals("Host")) {
                    ready_host = isChecked;
                    sw_host.setEnabled(!isChecked);
                    try {
                        Utils.sendHostMessage(host_socket, "Ready", String.valueOf(isChecked));
                    } catch (Exception ignored) {}
                }
                else if (multiplayer_mode.equals("Client")) {
                    ready_client = isChecked;
                    sw_client.setEnabled(!isChecked);
                    try {
                        Utils.sendClientMessage(client_socket, "Ready", String.valueOf(isChecked));
                    } catch (Exception ignored) {}
                }
            }
        });

        if (multiplayer_mode.equals("Host")) {
            ckbx_ready.setEnabled(false);
            try {
                host_socket = new Socket("192.168.1.5",8820);
                Utils.sendInitHostMessage(host_socket, host_name, "192.168.1.5");

                sw_client.setEnabled(false);
                server_address = Utils.getIPAddress(true);
                tv_room_ip.setText(server_address);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Couldn't initialize room\nTry again...", Toast.LENGTH_LONG).show();
                try {
                    host_socket.close();
                } catch (Exception ignored){
                }
            }
        } else if (multiplayer_mode.equals("Client")) {
            sw_host.setEnabled(false);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void checkClient() {
        if (multiplayer_mode.equals("Host")) {
            try {

            } catch (Exception ignored) {
                Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (!host_socket.isClosed())
                host_socket.close();
            if (!client_socket.isClosed())
                client_socket.close();
        } catch (Exception ignored) {
        } finally {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        try {
            if (!host_socket.isClosed())
                host_socket.close();
            if (!client_socket.isClosed())
                client_socket.close();
        } catch (Exception ignored) {
        } finally {
                finish();
        }
    }
}
