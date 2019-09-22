package com.example.tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Socket;

public class JoinMultiplayer extends AppCompatActivity implements View.OnClickListener, TextWatcher {

    Button btn_return_join;
    Button btn_join;

    EditText et_name_join;
    EditText et_ip;

    Socket client_socket;

    String client_name, lobby_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_multiplayer);

        btn_return_join = findViewById(R.id.btn_return_join);
        btn_join = findViewById(R.id.btn_join);
        et_name_join = findViewById(R.id.et_name_join);
        et_ip = findViewById(R.id.et_ip);

        btn_join.setOnClickListener(this);
        btn_return_join.setOnClickListener(this);

        et_name_join.addTextChangedListener(this);
        et_ip.addTextChangedListener(this);

        btn_join.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (view == this.btn_return_join) {
            try {
                client_socket.close();
                finish();
            } catch (Exception e) {
                finish();
            }
        } else if (view == this.btn_join) {
            try {
                lobby_ip = et_ip.getText().toString();
                client_name = et_name_join.getText().toString();

                ConnectSocket cs = new ConnectSocket(client_name);
                cs.execute("192.168.1.5", "8820");

                //client_socket = new Socket(lobby_ip, 8820);
                //Utils.sendInitClientMessage(client_socket, client_name);
                Toast.makeText(getApplicationContext(), "Succesfully joined lobby!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                try {
                    client_socket.close();
                } catch (Exception ignored) {
                } finally {
//                    Toast.makeText(getApplicationContext(), "Couldn't join the room\nMake sure that the ip is correct...", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (et_name_join.getText().length() == 0 || et_ip.getText().length() == 0)
            btn_join.setEnabled(false);
        else
            btn_join.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}

class ConnectSocket extends AsyncTask<String, Void, Socket> {
    private String name;
    public ConnectSocket(String name) {
        this.name = name;
    }

    public Socket doInBackground(String... params) {
        try {
            Socket socket = new Socket(params[0], Integer.parseInt(params[1]));
            Utils.sendInitClientMessage(socket, name);
            return socket;
        } catch (Exception ignored) {
            throw new Exception();
        } finally {
            return null;
        }
    }

}
