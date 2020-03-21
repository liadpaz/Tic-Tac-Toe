package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.liadpaz.tic_tac_toe.databinding.ActivityOptionsBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import static com.liadpaz.tic_tac_toe.Constants.LOBBIES;

public class OptionsActivity extends AppCompatActivity {

    private static final int CAMERA_ACTIVITY = 968;

    private File photo;
    private boolean photoOk = false;

    private int max_games = 1;
    private int timer = 0;
    private Utils.Mode mode = Utils.Mode.Computer;
    private Cell.Type starting_player = Cell.Type.X;
    private boolean difficulty = false;

    private NumberPicker numpic_timer;

    private EditText et_name_host;
    private boolean nameOk = false;

    private String lobbyNumber;

    private Button btn_play;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOptionsBinding binding = ActivityOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarOptions);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.options);

        mode = (Utils.Mode)getIntent().getSerializableExtra("Mode");

        NumberPicker numpic_maxgames = binding.numpicMaxgames;
        numpic_timer = binding.numpicTimer;
        CheckBox checkbox_timer = binding.chbxTimer;
        CheckBox checkbox_google_name = binding.checkboxGoogleName;
        btn_play = binding.btnPlay;
        Button btn_camera = binding.btnSettingsCamera;
        et_name_host = binding.etNameHost;
        TextView tv_name_host = binding.tvNameHost;
        TextView tv_singleplayer_mode = binding.tvSingleplayerMode;
        TextView tv_setting_computer = binding.tvSettingComputer;
        TextView tv_setting_two_players = binding.tvSettingTwoPlayers;
        Switch sw_singleplayer_mode = binding.swSingleplayerMode;
        Switch sw_player_start = binding.swPlayerStart;
        TextView tv_easy = binding.tvEasy;
        TextView tv_hard = binding.tvHard;
        Switch sw_difficulty = binding.swDifficulty;
        TextView tv_difficulty = binding.tvDifficulty;

        checkbox_google_name.setChecked(Stats.getGoogleName());

        numpic_maxgames.setMinValue(1);
        numpic_maxgames.setMaxValue(10);
        numpic_timer.setMinValue(2);
        numpic_timer.setMaxValue(10);

        numpic_maxgames.setOnValueChangedListener((picker, oldVal, newVal) -> max_games = newVal);
        numpic_timer.setOnValueChangedListener((picker, oldVal, newVal) -> timer = newVal);

        sw_player_start.setOnCheckedChangeListener((buttonView, isChecked) -> starting_player = isChecked ? Cell.Type.O : Cell.Type.X);
        sw_difficulty.setOnCheckedChangeListener((buttonView, isChecked) -> difficulty = isChecked);

        btn_play.setOnClickListener(v -> {
            if (mode == Utils.Mode.Multiplayer) {
                initializeLobby();
            } else {
                startActivity(new Intent(OptionsActivity.this, GameActivity.class).putExtra("Mode", mode).putExtra("Max", max_games).putExtra("Timer", timer).putExtra("Starting", starting_player).putExtra("Difficulty", difficulty));
            }
        });

        checkbox_timer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            numpic_timer.setEnabled(isChecked);
            timer = isChecked ? timer : 0;
        });
        checkbox_google_name.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            Stats.setGoogleName(isChecked);
            et_name_host.setText(isChecked ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "");
            et_name_host.setEnabled(!isChecked);
        }));

        numpic_maxgames.setValue(max_games);
        numpic_timer.setValue(timer);
        numpic_timer.setEnabled(false);

        if (mode == Utils.Mode.Multiplayer) {
            et_name_host.setVisibility(View.VISIBLE);
            checkbox_google_name.setVisibility(View.VISIBLE);
            tv_name_host.setVisibility(View.VISIBLE);
            if (!Stats.readPrivacy()) {
                btn_camera.setVisibility(View.VISIBLE);
            }
            photo = new File(getFilesDir(), "Photo.jpg");
            if (!Stats.getGooglePhoto()) {
                btn_camera.setOnClickListener(v -> {
                    Utils.localPhotoUri = FileProvider.getUriForFile(OptionsActivity.this, "com.liadpaz.tic_tac_toe.fileprovider", photo);
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Utils.localPhotoUri), CAMERA_ACTIVITY);
                });
            } else {
                new GooglePhotoTask(OptionsActivity.this, photo).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                btn_camera.setVisibility(View.INVISIBLE);
            }
            et_name_host.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 0) {
                        nameOk = false;
                        btn_play.setEnabled(false);
                    } else {
                        nameOk = true;
                        btn_play.setEnabled(photoOk || Stats.readPrivacy());
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    nameOk = s.length() > 0;
                }
            });
            btn_play.setEnabled(false);
        } else {
            tv_singleplayer_mode.setVisibility(View.VISIBLE);
            tv_setting_two_players.setVisibility(View.VISIBLE);
            tv_setting_computer.setVisibility(View.VISIBLE);
            sw_singleplayer_mode.setVisibility(View.VISIBLE);
            sw_singleplayer_mode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                mode = isChecked ? Utils.Mode.TwoPlayer : Utils.Mode.Computer;
                tv_difficulty.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
                tv_easy.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
                tv_hard.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
                sw_difficulty.setVisibility(mode == Utils.Mode.Computer ? View.VISIBLE : View.INVISIBLE);
            });
            mode = Utils.Mode.Computer;
            tv_difficulty.setVisibility(View.VISIBLE);
            tv_easy.setVisibility(View.VISIBLE);
            tv_hard.setVisibility(View.VISIBLE);
            sw_difficulty.setVisibility(View.VISIBLE);
            btn_play.setEnabled(true);
        }

        if (Stats.getGoogleName() && mode == Utils.Mode.Multiplayer) {
            nameOk = true;
            et_name_host.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            et_name_host.setEnabled(false);
        }
    }

    /**
     * This function initializes a lobby
     */
    private void initializeLobby() {
        DatabaseReference settingRef = Firebase.dataRef.child(LOBBIES);
        settingRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                for (String number : Utils.getRoomNumber()) {
                    if (!currentData.hasChild(number)) {
                        lobbyNumber = number;

                        currentData.child(lobbyNumber).setValue(new Lobby(et_name_host.getText().toString(), false, max_games, Stats.readPrivacy(), starting_player.toString(), timer));
                        currentData.child(lobbyNumber).child(Constants.STARTING_TYPE).setValue(starting_player.toString());

                        return Transaction.success(currentData);
                    }
                }
                return Transaction.abort();
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    startActivity(new Intent(OptionsActivity.this, LobbyActivity.class).putExtra(Constants.HOST_NAME, et_name_host.getText().toString()).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.HOST).putExtra(Constants.STARTING_EXTRA, starting_player).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber));
                } else {
                    Toast.makeText(OptionsActivity.this, R.string.lobby_couldnt_make, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_ACTIVITY && resultCode == RESULT_OK) {
            photoOk = true;
            btn_play.setEnabled(nameOk);
        }
    }

    /**
     * This class is for downloading the user's photo from his google account and storing it
     * locally, if it has failed to do so it will store a placeholder photo instead.
     */
    private static class GooglePhotoTask extends AsyncTask<String, Void, Void> {

        private WeakReference<OptionsActivity> optionsActivity;
        private File photo;

        GooglePhotoTask(OptionsActivity optionsActivity, File photo) {
            this.optionsActivity = new WeakReference<>(optionsActivity);
            this.photo = photo;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                InputStream is = url.openStream();
                OutputStream os = new FileOutputStream(photo);

                byte[] b = new byte[2048];
                int length;

                while ((length = is.read(b)) != -1) {
                    os.write(b, 0, length);
                }

                is.close();
                os.close();

                Utils.localPhotoUri = FileProvider.getUriForFile(optionsActivity.get(), "com.liadpaz.tic_tac_toe.fileprovider", photo);
            } catch (Exception ignored) {
                optionsActivity.get().runOnUiThread(() -> Toast.makeText(optionsActivity.get(), R.string.photo_not_found, Toast.LENGTH_LONG).show());

                try {
                    OutputStream os = new FileOutputStream(photo);
                    Bitmap bitmap = BitmapFactory.decodeResource(optionsActivity.get().getResources(), R.drawable.placeholder);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Bitmap.DENSITY_NONE, os);
                    os.close();
                } catch (Exception ignored1) {
                }
            } finally {
                optionsActivity.get().photoOk = true;
                if (optionsActivity.get().nameOk) {
                    optionsActivity.get().runOnUiThread(() -> optionsActivity.get().btn_play.setEnabled(true));
                }
            }
            return null;
        }
    }
}
