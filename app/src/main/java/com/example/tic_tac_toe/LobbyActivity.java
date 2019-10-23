package com.example.tic_tac_toe;

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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LobbyActivity extends AppCompatActivity {

    protected Button btn_exit_lobby;

    protected TextView tv_host_name;
    protected TextView tv_client_name;
    protected TextView tv_room_ip;

    protected Switch sw_host;
    protected Switch sw_client;

    protected CheckBox ckbx_ready;

    protected String multiplayer_mode = "";
    protected String server_address;
    protected String host_name;
    protected String client_name = null;

    protected boolean ready_host = false, ready_client = false;
    protected boolean type_host = false, type_client = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        multiplayer_mode = getIntent().getStringExtra("Multiplayer");
        host_name = getIntent().getStringExtra("HostName");

        btn_exit_lobby = findViewById(R.id.btn_exit_lobby);
        tv_host_name = findViewById(R.id.tv_host_name);
        tv_client_name = findViewById(R.id.tv_client_name);
        tv_room_ip = findViewById(R.id.tv_room_ip);
        sw_host = findViewById(R.id.sw_host_side);
        sw_client = findViewById(R.id.sw_client_side);
        ckbx_ready = findViewById(R.id.ckbx_ready);

        tv_host_name.setText(host_name);
        tv_client_name.setText("Waiting...");
        ckbx_ready.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (multiplayer_mode.equals("Host")) {
                    ready_host = isChecked;
                    sw_host.setEnabled(!isChecked);
                }
            }
        });
        btn_exit_lobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LobbyActivity.this.finish();
            }
        });

        if (multiplayer_mode.equals("Host")) {
            ckbx_ready.setEnabled(false);
            try {

                sw_client.setEnabled(false);
                server_address = Utils.getIPAddress(true);
                tv_room_ip.setText(server_address);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString()/*"Couldn't initialize room\nTry again..."*/, Toast.LENGTH_LONG).show();
                finish();
            }
        } else /* if (multiplayer_mode.equals("Client")) */ {
            sw_host.setEnabled(false);
            sw_client.setEnabled(false);
        }
    }
}
