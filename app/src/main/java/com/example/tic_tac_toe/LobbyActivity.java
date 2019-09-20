package com.example.tic_tac_toe;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.net.InetSocketAddress;
import java.net.Socket;

public class LobbyActivity extends AppCompatActivity {

    Socket mSocket;

    TextView tv_host_name;
    TextView tv_client_name;

    Switch sw_host;
    Switch sw_client;

    String multiplayer_mode = "";
    String server_addr;
    String host_name;
    String client_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        multiplayer_mode = getIntent().getStringExtra("Multiplayer");

        tv_host_name = findViewById(R.id.tv_host_name);
        tv_client_name = findViewById(R.id.tv_client_name);

        host_name = getIntent().getStringExtra("HostName");
        tv_host_name.setText(host_name);

        if (multiplayer_mode.equals("Host"))
        {
            try
            {
                mSocket = new Socket();
                mSocket.bind(new InetSocketAddress(Utils.getIPAddress(true), 1337));
                server_addr = Utils.getIPAddress(true);
                //Toast.makeText(getApplicationContext(), Utils.getIPAddress(true), Toast.LENGTH_LONG).show();
            }
            catch (Exception e1)
            {
                Toast.makeText(getApplicationContext(), "Couldn't initialize room\nTry again...", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mSocket.close();
        } catch (Exception e){} finally {
            finish();
        }
    }
}
