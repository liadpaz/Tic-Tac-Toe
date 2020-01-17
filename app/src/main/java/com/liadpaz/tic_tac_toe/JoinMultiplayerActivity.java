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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Objects;

public class JoinMultiplayerActivity extends AppCompatActivity {

    private static final int PHOTO_ACTIVITY = 1;

    DatabaseReference joinRef;

    File photo;

    Button btn_join;
    Button btn_camera;

    EditText et_name_join;
    EditText et_lobby_number;

    String clientName;
    String lobbyNumber;

    CheckBox ckbx_google_name_join;

    boolean photoOk = false;
    boolean nameOk = false;
    boolean numberOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_multiplayer);
        setSupportActionBar(findViewById(R.id.toolbar_join));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.join_lobby);

        joinRef = Firebase.dataRef.child("Lobbies");

        btn_join = findViewById(R.id.btn_join);
        btn_camera = findViewById(R.id.btn_join_camera);
        et_name_join = findViewById(R.id.et_name_join);
        et_lobby_number = findViewById(R.id.et_lobby_number);
        ckbx_google_name_join = findViewById(R.id.ckbx_google_name_join);

        ckbx_google_name_join.setChecked(Stats.getGoogleName());

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
                            if (dataSnapshot.child(lobbyNumber).child("clientName").getValue(String.class) == null) {
                                startActivity(new Intent(JoinMultiplayerActivity.this, LobbyActivity.class)
                                        .putExtra("ClientName", clientName)
                                        .putExtra("Multiplayer", "Client")
                                        .putExtra("LobbyNumber", lobbyNumber));
                                joinRef.child(lobbyNumber).child("clientName").setValue(clientName);
                                if (!Objects.requireNonNull(dataSnapshot.child(lobbyNumber).child("privacy").getValue(Boolean.class))) {
                                    joinRef.child(lobbyNumber).child("privacy").setValue(Stats.readPrivacy());
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
            photo = new File(JoinMultiplayerActivity.this.getFilesDir(), "Photo.jpg");
            if (!Stats.getGooglePhoto()) {
                btn_camera.setOnClickListener(v -> {
                    Utils.localPhotoUri = FileProvider.getUriForFile(JoinMultiplayerActivity.this, "com.liadpaz.tic_tac_toe.fileprovider", photo);
                    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Utils.localPhotoUri), PHOTO_ACTIVITY);
                });
            } else {
                new PhotoTask(JoinMultiplayerActivity.this, photo).execute(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhotoUrl()).toString());
                btn_camera.setVisibility(View.INVISIBLE);
            }
        }

        ckbx_google_name_join.setOnCheckedChangeListener((buttonView, isChecked) -> {
            nameOk = isChecked;
            Stats.setGoogleName(isChecked);
            et_name_join.setText(isChecked ? Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName() : "");
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
            et_name_join.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getDisplayName());
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

    private static class PhotoTask extends AsyncTask<String, Void, Void> {

        private WeakReference<JoinMultiplayerActivity> joinMultiplayer;
        private File photo;

        PhotoTask(JoinMultiplayerActivity joinMultiplayerActivity, File photo) {
            this.joinMultiplayer = new WeakReference<>(joinMultiplayerActivity);
            this.photo = photo;
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                URL url = new URL(Objects.requireNonNull(strings[0]));
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
                    Bitmap bitmap = BitmapFactory.decodeResource(joinMultiplayer.get().getResources(), R.drawable.placeholder);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, Bitmap.DENSITY_NONE, os);
                    os.close();
                } catch (Exception ignored1) {}
            } finally {
                joinMultiplayer.get().photoOk = true;
            }
            return null;
        }
    }
}

