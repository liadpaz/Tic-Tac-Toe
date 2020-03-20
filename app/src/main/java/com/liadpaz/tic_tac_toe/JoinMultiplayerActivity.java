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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.liadpaz.tic_tac_toe.databinding.ActivityJoinMultiplayerBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

public class JoinMultiplayerActivity extends AppCompatActivity {

    private static final int PHOTO_ACTIVITY = 1;

    private DatabaseReference joinRef;

    private File photo;

    private Button btn_join;

    private EditText et_name_join;
    private EditText et_lobby_number;

    private String clientName;
    private String lobbyNumber;

    private boolean photoOk = false;
    private boolean nameOk = false;
    private boolean numberOk = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityJoinMultiplayerBinding binding = ActivityJoinMultiplayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarJoin);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.join_lobby);

        joinRef = Firebase.dataRef.child("Lobbies");

        btn_join = binding.btnJoin;
        Button btn_camera = binding.btnJoinCamera;
        et_name_join = binding.etNameJoin;
        et_lobby_number = binding.etLobbyNumber;
        CheckBox checkbox_google_name_join = binding.checkboxGoogleNameJoin;

        checkbox_google_name_join.setChecked(Stats.getGoogleName());

        btn_join.setOnClickListener(v -> {
            clientName = et_name_join.getText().toString();
            lobbyNumber = et_lobby_number.getText().toString();

            if (lobbyNumber.length() != 4) {
                Toast.makeText(JoinMultiplayerActivity.this, R.string.lobby_length, Toast.LENGTH_LONG).show();
            } else {
                joinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(lobbyNumber)) {
                            if (!dataSnapshot.child(lobbyNumber).hasChild("clientName")) {
                                startActivity(new Intent(JoinMultiplayerActivity.this, LobbyActivity.class).putExtra("ClientName", clientName).putExtra(GameActivity.MULTIPLAYER_EXTRA, "Client").putExtra(GameActivity.LOBBY_NUMBER_EXTRA, lobbyNumber));
                                joinRef.child(lobbyNumber).child("clientName").setValue(clientName);
                                if (!dataSnapshot.child(lobbyNumber + "/privacy").getValue(Boolean.class)) {
                                    joinRef.child(lobbyNumber + "/privacy").setValue(Stats.readPrivacy());
                                }
                            } else {
                                Toast.makeText(JoinMultiplayerActivity.this, R.string.lobby_full, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(JoinMultiplayerActivity.this, R.string.lobby_not_found, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {}
                });
            }
        });
        if (!Stats.readPrivacy()) {
            btn_camera.setVisibility(View.VISIBLE);
            photo = new File(getFilesDir(), "PhotoLocal.jpg");
            if (!Stats.getGooglePhoto()) {
                btn_camera.setOnClickListener(v -> {
                    Utils.localPhotoUri = FileProvider.getUriForFile(JoinMultiplayerActivity.this, "com.liadpaz.tic_tac_toe.fileprovider", photo);
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Utils.localPhotoUri), PHOTO_ACTIVITY);
                });
            } else {
                new GooglePhotoTask(JoinMultiplayerActivity.this, photo).execute(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString());
                btn_camera.setVisibility(View.INVISIBLE);
            }
        }

        checkbox_google_name_join.setOnCheckedChangeListener((buttonView, isChecked) -> {
            nameOk = isChecked;
            Stats.setGoogleName(isChecked);
            et_name_join.setText(isChecked ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "");
            et_name_join.setEnabled(!isChecked);
        });

        et_name_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameOk = s.length() > 0;
                btn_join.setEnabled(nameOk && numberOk && (photoOk || Stats.readPrivacy()));
            }

            @Override
            public void afterTextChanged(Editable s) {
                nameOk = s.length() > 0;
            }
        });
        et_lobby_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberOk = s.length() > 0;
                btn_join.setEnabled(numberOk && nameOk && (photoOk || Stats.readPrivacy()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (Stats.getGoogleName()) {
            nameOk = true;
            et_name_join.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            et_name_join.setEnabled(false);
        }

        btn_join.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PHOTO_ACTIVITY && resultCode == RESULT_OK) {
            photoOk = true;
            if (nameOk && et_lobby_number.getText().length() > 0) {
                btn_join.setEnabled(true);
            }
        }
    }

    /**
     * This class is for downloading the player's photo to the device. It stores a weak reference of
     * the activity
     */
    private static class GooglePhotoTask extends AsyncTask<String, Void, Void> {

        private WeakReference<JoinMultiplayerActivity> joinMultiplayer;
        private File photo;

        GooglePhotoTask(JoinMultiplayerActivity joinMultiplayerActivity, File photo) {
            this.joinMultiplayer = new WeakReference<>(joinMultiplayerActivity);
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

                Utils.localPhotoUri = FileProvider.getUriForFile(joinMultiplayer.get(), "com.liadpaz.tic_tac_toe.fileprovider", photo);
            } catch (Exception ignored) {
                Toast.makeText(joinMultiplayer.get(), R.string.photo_not_found, Toast.LENGTH_LONG).show();
                try {
                    OutputStream os = new FileOutputStream(photo);
                    BitmapFactory.decodeResource(joinMultiplayer.get().getResources(), R.drawable.placeholder).compress(Bitmap.CompressFormat.JPEG, Bitmap.DENSITY_NONE, os);
                    os.close();
                } catch (Exception ignored1) {}
            } finally {
                joinMultiplayer.get().photoOk = true;
            }
            return null;
        }
    }
}