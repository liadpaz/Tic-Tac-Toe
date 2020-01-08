package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    DatabaseReference settingRef;

    File photo;
    boolean photoOk = false;

    int max_games = 1;
    int timer = 0;
    Utils.Mode mode = Utils.Mode.Computer;
    Cell.Type starting_player = Cell.Type.X;
    boolean difficulty = false;

    NumberPicker numpic_maxgames;
    NumberPicker numpic_timer;

    Switch sw_player_start;

    TextView tv_setting_computer;
    TextView tv_setting_two_players;
    TextView tv_singleplayer_mode;
    Switch sw_singleplayer_mode;

    TextView tv_difficulty;
//    TextView tv_easy;
//    TextView tv_hard;
    Switch sw_difficulty;

    TextView tv_name_host;
    EditText et_name_host;
    boolean nameOk = false;
    CheckBox ckbx_google_name;

    CheckBox ckbx_timer;

    Button btn_play;
    Button btn_camera;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar(findViewById(R.id.toolbar_setting));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.settings);

        mode = (Utils.Mode) getIntent().getSerializableExtra("Mode");

        numpic_maxgames = findViewById(R.id.numpic_maxgames);
        numpic_timer = findViewById(R.id.numpic_timer);
        ckbx_timer = findViewById(R.id.chbx_timer);
        ckbx_google_name = findViewById(R.id.ckbx_google_name);
        btn_play = findViewById(R.id.btn_play);
        btn_camera = findViewById(R.id.btn_settings_camera);
        et_name_host = findViewById(R.id.et_name_host);
        tv_name_host = findViewById(R.id.tv_name_host);
        tv_singleplayer_mode = findViewById(R.id.tv_singleplayer_mode);
        tv_setting_computer = findViewById(R.id.tv_setting_computer);
        tv_setting_two_players = findViewById(R.id.tv_setting_two_players);
        sw_singleplayer_mode = findViewById(R.id.sw_singleplayer_mode);
        sw_player_start = findViewById(R.id.sw_player_start);
        sw_difficulty = findViewById(R.id.sw_difficulty);
        tv_difficulty = findViewById(R.id.tv_difficulty);
//        tv_easy = findViewById(R.id.tv_easy);
//        tv_hard = findViewById(R.id.tv_hard);

        ckbx_google_name.setChecked(Stats.getGoogleName());

        numpic_maxgames.setMinValue(1);
        numpic_maxgames.setMaxValue(10);
        numpic_timer.setMinValue(2);
        numpic_timer.setMaxValue(10);

        numpic_maxgames.setOnValueChangedListener((picker, oldVal, newVal) -> max_games = newVal);
        numpic_timer.setOnValueChangedListener((picker, oldVal, newVal) -> timer = newVal);

        sw_player_start.setOnCheckedChangeListener((buttonView, isChecked) -> starting_player = isChecked ? Cell.Type.O : Cell.Type.X);
        sw_difficulty.setOnCheckedChangeListener((buttonView, isChecked) -> difficulty = isChecked);

        ckbx_timer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            numpic_timer.setEnabled(isChecked);
            if (!isChecked) {
                timer = 0;
            } else {
                timer = numpic_timer.getValue();
            }
        });
        ckbx_google_name.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Stats.setGoogleName(isChecked);
            et_name_host.setText(isChecked ? Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName() : "");
            et_name_host.setEnabled(!isChecked);
        }));

        btn_play.setOnClickListener(v -> {
            if (mode == Utils.Mode.Multiplayer) {
                SettingsActivity.this.initializeLobby();
            } else {
                SettingsActivity.this.startActivity(new Intent(SettingsActivity.this.getApplicationContext(), Game.class)
                        .putExtra("Mode", mode)
                        .putExtra("Max", max_games)
                        .putExtra("Timer", timer)
                        .putExtra("Starting", starting_player)
                        .putExtra("Difficulty", difficulty));
            }
        });
        btn_camera.setOnClickListener(v -> {
            photo = new File(SettingsActivity.this.getFilesDir(), "Photo.jpg");
            Utils.localPhotoUri = FileProvider.getUriForFile(SettingsActivity.this, "com.liadpaz.tic_tac_toe.fileprovider", photo);
            SettingsActivity.this.startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Utils.localPhotoUri), 0);
        });

        et_name_host.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() == 0) {
                    nameOk = false;
                    btn_play.setEnabled(false);
                } else {
                    nameOk = true;
                    btn_play.setEnabled(photoOk || Stats.readPrivacy());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                nameOk = editable.length() > 0;
            }
        });

        numpic_maxgames.setValue(max_games);
        numpic_timer.setValue(timer);
        numpic_timer.setEnabled(false);

        if (mode == Utils.Mode.Multiplayer) {
            et_name_host.setVisibility(View.VISIBLE);
            ckbx_google_name.setVisibility(View.VISIBLE);
            tv_name_host.setVisibility(View.VISIBLE);
            if (!Stats.readPrivacy()) {
                btn_camera.setVisibility(View.VISIBLE);
            }
            btn_play.setEnabled(false);
        } else {
            tv_singleplayer_mode.setVisibility(View.VISIBLE);
            tv_setting_two_players.setVisibility(View.VISIBLE);
            tv_setting_computer.setVisibility(View.VISIBLE);
            sw_singleplayer_mode.setVisibility(View.VISIBLE);
            sw_singleplayer_mode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mode = isChecked ? Utils.Mode.TwoPlayer : Utils.Mode.Computer;
//                    tv_difficulty.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
//                    tv_easy.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
//                    tv_hard.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
//                    sw_difficulty.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
            });
            mode = Utils.Mode.Computer;
//            tv_difficulty.setVisibility(View.VISIBLE);
//            tv_easy.setVisibility(View.VISIBLE);
//            tv_hard.setVisibility(View.VISIBLE);
//            sw_difficulty.setVisibility(View.VISIBLE);
            btn_play.setEnabled(true);
        }

        if (Stats.getGoogleName() && mode == Utils.Mode.Multiplayer) {
            nameOk = true;
            et_name_host.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
            et_name_host.setEnabled(false);
        }
    }

    /**
     * This function initializes a lobby
     */
    private void initializeLobby() {
        settingRef = Firebase.dataRef.child("Lobbies");
        settingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lobbyNumber = Utils.getRoomNumber();

                while (dataSnapshot.hasChild(lobbyNumber)) {
                    lobbyNumber = Utils.getRoomNumber();
                }

                settingRef.child(lobbyNumber).setValue(new Lobby(et_name_host.getText().toString(), lobbyNumber, starting_player.toString(), timer, max_games, Stats.readPrivacy()));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            photoOk = true;
            btn_play.setEnabled(nameOk);
        }
    }
}
