package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.liadpaz.tic_tac_toe.databinding.ActivityLobbyBinding;

import org.jetbrains.annotations.NotNull;

import static com.liadpaz.tic_tac_toe.Constants.LOBBIES;
import static com.liadpaz.tic_tac_toe.Constants.NOT_READY;
import static com.liadpaz.tic_tac_toe.Constants.READY;
import static com.liadpaz.tic_tac_toe.Stats.readPrivacy;

public class LobbyActivity extends AppCompatActivity {

    private DatabaseReference lobbyRef;

    private Integer max;
    private Integer timer;

    private boolean isHost;

    private TextView tv_host_name;
    private TextView tv_client_name;

    private Switch sw_host;
    private Switch sw_client;
    private CheckBox checkBox_host_ready;
    private CheckBox checkBox_client_ready;

    private CheckBox checkbox_ready;

    private String lobbyNumber;
    private String hostName;
    private String clientName;

    private TextView tv_uploading_photo;
    private ProgressBar progressBar_uploading_photo;

    private ValueEventListener listener;

    private boolean ready_host = false;
    private boolean ready_client = false;
    private boolean ready_photo = true;

    private UploadTask uploadPhotoTask;

    private boolean isLaunchingGame = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLobbyBinding binding = ActivityLobbyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        lobbyNumber = getIntent().getStringExtra(Constants.LOBBY_NUMBER_EXTRA);
        isHost = Constants.HOST.equals(getIntent().getStringExtra(Constants.MULTIPLAYER_EXTRA));
        hostName = getIntent().getStringExtra(Constants.HOST_NAME);

        binding.btnExitLobby.setOnClickListener(v -> {
            lobbyRef.removeValue();
            finish();
        });
        tv_host_name = binding.tvHostName;
        tv_client_name = binding.tvClientName;
        binding.tvRoomNumber.setText(lobbyNumber);
        sw_host = binding.swHostSide;
        sw_client = binding.swClientSide;
        checkBox_host_ready = binding.checkBoxHostReady;
        checkBox_client_ready = binding.checkBoxClientReady;
        checkbox_ready = binding.checkboxReady;
        tv_uploading_photo = binding.tvUploadingPhoto;
        progressBar_uploading_photo = binding.progressBarUploadingPhoto;

        tv_host_name.setText(hostName);
        tv_client_name.setText(R.string.waiting);

        sw_client.setChecked(true);

        if (isHost) {
            checkBox_host_ready.setVisibility(View.INVISIBLE);
            sw_host.setOnCheckedChangeListener((buttonView, isChecked) -> {
                sw_client.setChecked(!isChecked);
                setHostType(isChecked);
            });
            sw_client.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                sw_host.setChecked(!isChecked);
                setHostType(!isChecked);

            }));
        } else {
            checkBox_client_ready.setVisibility(View.INVISIBLE);
            sw_host.setClickable(false);
        }

        checkbox_ready.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isHost) {
                writeDatabaseMessage(isChecked ? READY : NOT_READY);
                ready_host = isChecked;
                sw_host.setClickable(!isChecked);
                if (ready_host && ready_client) {
                    isLaunchingGame = true;
                    writeDatabaseMessage(Constants.PLAY);
                    startActivity(new Intent(LobbyActivity.this, GameActivity.class).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.HOST));

                }
            } else {
                writeDatabaseMessage(isChecked ? READY : NOT_READY);
            }
        });

        checkbox_ready.setEnabled(!isHost);

        lobbyRef = Firebase.dataRef.child(LOBBIES).child(lobbyNumber);

        lobbyRef.onDisconnect().removeValue();

        listener = lobbyRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.hasChildren()) {
                    new AlertDialog.Builder(LobbyActivity.this).setMessage(R.string.lobby_left_message).setPositiveButton(R.string.back_main_menu, (dialog, which) -> {
                        lobbyRef.removeValue();
                        finishAffinity();
                        startActivity(new Intent(LobbyActivity.this, MainActivity.class));
                    }).setCancelable(false).show();
                    return;
                }
                if (max == null || timer == null) {
                    max = dataSnapshot.child(Constants.MAX).getValue(Integer.class);
                    timer = dataSnapshot.child(Constants.TIMER).getValue(Integer.class);
                }
                if (isHost) {
                    if (clientName == null) {
                        if ((clientName = dataSnapshot.child(Constants.CLIENT_NAME).getValue(String.class)) != null) {
                            tv_client_name.setText(clientName);
                            checkbox_ready.setEnabled(ready_photo);
                        }
                    } else {
                        String clientMessage = dataSnapshot.child(Constants.CLIENT_MESSAGE).getValue(String.class);
                        if (READY.equals(clientMessage)) {
                            ready_client = true;
                            if (ready_host && ready_photo) {
                                isLaunchingGame = true;
                                writeDatabaseMessage(Constants.PLAY);
                                startActivity(new Intent(LobbyActivity.this, GameActivity.class).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.HOST));
                                finish();
                            }
                        } else /* if ("not_ready".equals(clientMessage)) */ {
                            ready_client = false;
                        }
                        checkBox_client_ready.setChecked(ready_client);
                    }
                } else {
                    if (hostName == null) {
                        hostName = dataSnapshot.child(Constants.HOST_NAME).getValue(String.class);
                        clientName = dataSnapshot.child(Constants.CLIENT_NAME).getValue(String.class);
                        tv_host_name.setText(hostName);
                        tv_client_name.setText(clientName);
                    }

                    String hostType;
                    if ((hostType = dataSnapshot.child(Constants.HOST_TYPE).getValue(String.class)) != null) {
                        swapSwitches(hostType);
                    }

                    String hostMessage = dataSnapshot.child(Constants.HOST_MESSAGE).getValue(String.class);
                    if (Constants.PLAY.equals(hostMessage)) {
                        isLaunchingGame = true;
                        startActivity(new Intent(LobbyActivity.this, GameActivity.class).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.CLIENT));
                        finish();
                    } else {
                        ready_host = READY.equals(hostMessage);
                    }
                    checkBox_host_ready.setChecked(ready_host);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });

        if (!readPrivacy()) {
            uploadPhoto();
        }
    }

    /**
     * This function writes a message to the database based on the player type
     *
     * @param message the message to write
     */
    private void writeDatabaseMessage(String message) {
        if (isHost) {
            lobbyRef.child(Constants.HOST_MESSAGE).setValue(message);
        } else {
            lobbyRef.child(Constants.CLIENT_MESSAGE).setValue(message);
        }
    }

    /**
     * This function writes to the database the host type
     *
     * @param type <code>true</code> if the host is X, <code>false</code> if the host is O
     */
    private void setHostType(boolean type) {
        lobbyRef.child(Constants.HOST_TYPE).setValue(type ? "X" : "O");
    }

    /**
     * This function swap the types switches graphically
     *
     * @param hostType the type of the host
     */
    private void swapSwitches(@NotNull String hostType) {
        if ("X".equals(hostType)) {
            sw_host.setChecked(true);
            sw_client.setChecked(false);
        } else {
            sw_host.setChecked(false);
            sw_client.setChecked(true);
        }
    }

    /**
     * This function uploads the photo the player took to the Firebase Storage
     */
    private void uploadPhoto() {
        ready_photo = false;
        checkbox_ready.setEnabled(false);
        tv_uploading_photo.setVisibility(View.VISIBLE);
        progressBar_uploading_photo.setVisibility(View.VISIBLE);
        StorageReference storageRef = Firebase.storeRef.child(LOBBIES).child(lobbyNumber);
        (uploadPhotoTask = storageRef.child(isHost ? Constants.HOST : Constants.CLIENT).putFile(Utils.localPhotoUri)).addOnCompleteListener(task -> {
            ready_photo = true;
            checkbox_ready.setEnabled(clientName != null);
            tv_uploading_photo.setVisibility(View.INVISIBLE);
            progressBar_uploading_photo.setVisibility(View.INVISIBLE);
        });
    }

    @Override
    protected void onDestroy() {
        lobbyRef.removeEventListener(listener);
        if (!isLaunchingGame) {
            lobbyRef.removeValue();
            if (uploadPhotoTask != null) {
                uploadPhotoTask.cancel();
            }
        }
        super.onDestroy();
    }
}