package com.example.tic_tac_toe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener, NumberPicker.OnValueChangeListener, CompoundButton.OnCheckedChangeListener {

    int mode, max_games = 1, timer = 0;

    NumberPicker numpic_maxgames;
    NumberPicker numpic_timer;

    CheckBox tgl_timer;

    Button btn_return;
    Button btn_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mode = getIntent().getIntExtra("Mode", -1);

        max_games = getIntent().getIntExtra("Max", 1);
        timer = getIntent().getIntExtra("Timer", 5);

        numpic_maxgames = findViewById(R.id.numpic_games);
        numpic_maxgames.setMinValue(1);
        numpic_maxgames.setMaxValue(10);
        numpic_timer = findViewById(R.id.numpic_timer);
        numpic_timer.setMinValue(5);
        numpic_timer.setMaxValue(60);
        tgl_timer = findViewById(R.id.tgl_timer);
        btn_return = findViewById(R.id.btn_return);
        btn_play = findViewById(R.id.btn_play);

        numpic_maxgames.setOnValueChangedListener(this);
        numpic_timer.setOnValueChangedListener(this);

        tgl_timer.setOnCheckedChangeListener(this);

        btn_return.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        numpic_maxgames.setValue(max_games);
        numpic_timer.setValue(timer);
        numpic_timer.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (view == this.btn_return)
        {
            finish();
        }
        else if (view == this.btn_play)
        {
            if (mode == 0)
            {
                Intent singleplayer = new Intent(getApplicationContext(), Game.class);
                singleplayer.putExtra("Mode", 0).putExtra("Max", max_games).putExtra("Timer", timer);
                startActivity(singleplayer);
            }
            else if (mode == 1)
            {

            }
        }
    }

    @Override
    public void onValueChange(NumberPicker numberPicker, int i, int i1) {
        if (numberPicker == this.numpic_maxgames)
        {
            max_games = numberPicker.getValue();
        }
        else if (numberPicker == this.numpic_timer)
        {
            timer = numberPicker.getValue();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            numpic_timer.setEnabled(b);
            if (!b)
            {
                timer = 0;
            }
            else
            {
                timer = numpic_timer.getValue();
            }
    }
}
