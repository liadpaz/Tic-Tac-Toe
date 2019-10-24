package com.example.tic_tac_toe;

import androidx.annotation.NonNull;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class SettingsActivity extends AppCompatActivity {

    DatabaseReference settingRef;

    int max_games = 1;
    int timer = 0;
    Utils.Mode mode;
    Cell.Type starting_player = Cell.Type.X;

    NumberPicker numpic_maxgames;
    NumberPicker numpic_timer;

    TextView tv_player_start;
    Switch sw_player_start;

    TextView tv_name_host;
    EditText et_name_host;

    CheckBox sw_timer;

    Button btn_return;
    Button btn_play;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");

        numpic_maxgames = findViewById(R.id.numpic_games);
        numpic_timer = findViewById(R.id.numpic_timer);
        sw_timer = findViewById(R.id.sw_timer);
        btn_return = findViewById(R.id.btn_return_settigns);
        btn_play = findViewById(R.id.btn_play);
        et_name_host = findViewById(R.id.et_name_host);
        tv_name_host = findViewById(R.id.tv_name_host);
        tv_player_start = findViewById(R.id.tv_player_start);
        sw_player_start = findViewById(R.id.sw_player_start);

        numpic_maxgames.setMinValue(1);
        numpic_maxgames.setMaxValue(10);
        numpic_timer.setMinValue(2);
        numpic_timer.setMaxValue(10);

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


        sw_player_start.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) starting_player = Cell.Type.O;
                else starting_player = Cell.Type.X;
            }
        });
        sw_timer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    initializeLobby();
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

    private void initializeLobby() {

        settingRef = Database.dataRef.child("Lobbies");

        settingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String lobbyNumber = Utils.getRoomNumber();

                while (dataSnapshot.hasChild(lobbyNumber))
                    lobbyNumber = Utils.getRoomNumber();

                settingRef.child(lobbyNumber).setValue(new Lobby(et_name_host.getText().toString(), lobbyNumber, starting_player.toString(), timer, max_games));
                settingRef.child(lobbyNumber).child("startingType").setValue(starting_player.toString());

                startActivity(new Intent(SettingsActivity.this, LobbyActivity.class)
                        .putExtra("HostName", et_name_host.getText().toString())
                        .putExtra("Multiplayer", "Host")
                        .putExtra("Starting", starting_player)
                        .putExtra("LobbyNumber", lobbyNumber));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
