package com.example.tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class JoinMultiplayer extends AppCompatActivity implements TextWatcher {

    Button btn_return_join;
    Button btn_join;

    EditText et_name_join;
    EditText et_ip;

    String client_name, lobby_ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_multiplayer);

        btn_return_join = findViewById(R.id.btn_return_join);
        btn_join = findViewById(R.id.btn_join);
        et_name_join = findViewById(R.id.et_name_join);
        et_ip = findViewById(R.id.et_ip);

        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btn_return_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_name_join.addTextChangedListener(this);
        et_ip.addTextChangedListener(this);

        btn_join.setEnabled(false);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (et_name_join.getText().length() == 0 || et_ip.getText().length() == 0)
            btn_join.setEnabled(false);
        else
            btn_join.setEnabled(true);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}


