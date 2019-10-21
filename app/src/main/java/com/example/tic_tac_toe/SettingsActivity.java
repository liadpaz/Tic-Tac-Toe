package com.example.tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    int max_games = 1, timer = 0;
    Utils.Mode mode;
    Cell.Type starting_player = Cell.Type.X;

    NumberPicker numpic_maxgames;
    NumberPicker numpic_timer;

    TextView tv_player_start;
    Switch sw_player_start;

    TextView tv_name_host;
    EditText et_name_host;

    CheckBox tgl_timer;

    Button btn_return;
    Button btn_play;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");

        max_games = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 5);

        numpic_maxgames = findViewById(R.id.numpic_games);
        numpic_timer = findViewById(R.id.numpic_timer);
        tgl_timer = findViewById(R.id.tgl_timer);
        btn_return = findViewById(R.id.btn_return_settigns);
        btn_play = findViewById(R.id.btn_play);
        et_name_host = findViewById(R.id.et_name_host);
        tv_name_host = findViewById(R.id.tv_name_host);
        tv_player_start = findViewById(R.id.tv_player_start);
        sw_player_start = findViewById(R.id.sw_player_start);

        numpic_maxgames.setMinValue(1);
        numpic_maxgames.setMaxValue(10);
        numpic_timer.setMinValue(5);
        numpic_timer.setMaxValue(60);

        numpic_maxgames.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                max_games = newVal;
            }
        });
        numpic_timer.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                timer = newVal;
            }
        });

        tgl_timer.setEnabled(false);
        numpic_timer.setEnabled(false);
        tgl_timer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                numpic_timer.setEnabled(isChecked);
                if (!isChecked)
                    timer = 0;
                else
                    timer = numpic_timer.getValue();
            }
        });

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode == Utils.Mode.Computer) {
                    startActivity(new Intent(getApplicationContext(), Game.class)
                            .putExtra("Mode", Utils.Mode.Computer)
                            .putExtra("Max", max_games)
                            .putExtra("Timer", timer)
                            .putExtra("Starting", starting_player));
                } else if (mode == Utils.Mode.TwoPlayer) {
                    startActivity(new Intent(getApplicationContext(), Game.class)
                            .putExtra("Mode", Utils.Mode.TwoPlayer)
                            .putExtra("Max", max_games)
                            .putExtra("Timer", timer)
                            .putExtra("Starting", starting_player));

                } else if (mode == Utils.Mode.Multiplayer) {
                    startActivity(new Intent(getApplicationContext(), LobbyActivity.class)
                            .putExtra("Mode", Utils.Mode.Multiplayer)
                            .putExtra("Max", max_games)
                            .putExtra("Timer", timer)
                            .putExtra("HostName", et_name_host.getText())
                            .putExtra("Multiplayer", "Host")
                            .putExtra("Starting", starting_player));
                }
            }
        });
        btn_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        et_name_host.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0)
                    btn_play.setEnabled(false);
                else
                    btn_play.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        sw_player_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) starting_player = Cell.Type.O;
                else starting_player = Cell.Type.X;
            }
        });

        numpic_maxgames.setValue(max_games);
        numpic_timer.setValue(timer);
        numpic_timer.setEnabled(false);

        if (mode == Utils.Mode.Computer || mode == Utils.Mode.TwoPlayer)
            btn_play.setEnabled(true);
        else /*if (mode == Utils.Mode.Multiplayer)*/ {
            et_name_host.setVisibility(View.VISIBLE);
            tv_name_host.setVisibility(View.VISIBLE);
        }
    }
}
