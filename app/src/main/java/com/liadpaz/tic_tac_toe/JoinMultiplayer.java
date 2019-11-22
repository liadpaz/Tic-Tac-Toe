package com.liadpaz.tic_tac_toe;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.Objects;

public class JoinMultiplayer extends AppCompatActivity {

    DatabaseReference joinRef;

    File photo;

    Button btn_return_join;
    Button btn_join;
    Button btn_camera;

    EditText et_name_join;
    EditText et_lobby_number;

    String clientName;
    String lobbyNumber;

    boolean photoOk = false;
    boolean nameOk = false;
    boolean numberOk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_multiplayer);

        joinRef = Firebase.dataRef.child("Lobbies");

        btn_return_join = findViewById(R.id.btn_return_join);
        btn_join = findViewById(R.id.btn_join);
        btn_camera = findViewById(R.id.btn_join_camera);
        et_name_join = findViewById(R.id.et_name_join);
        et_lobby_number = findViewById(R.id.et_lobby_number);

        btn_return_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clientName = et_name_join.getText().toString();
                lobbyNumber = et_lobby_number.getText().toString();

                if (lobbyNumber.length() != 4) {
                    Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyLength), Toast.LENGTH_LONG).show();
                } else {
                    joinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot data : dataSnapshot.getChildren()) {
                                if (Objects.equals(data.getKey(), lobbyNumber)) {
                                    if (data.child("clientName").getValue(String.class) == null) {

                                        startActivity(new Intent(JoinMultiplayer.this, LobbyActivity.class)
                                                .putExtra("ClientName", clientName)
                                                .putExtra("Multiplayer", "Client")
                                                .putExtra("LobbyNumber", lobbyNumber));
                                        joinRef.child(lobbyNumber).child("clientName").setValue(clientName);
                                        if (!Objects.requireNonNull(data.child("privacy").getValue(Boolean.class))) {
                                            joinRef.child(lobbyNumber).child("privacy").setValue(Stats.readPrivacy());
                                        }
                                    } else {
                                        Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyFull), Toast.LENGTH_LONG).show();
                                    }
                                    return;
                                }
                            }
                            Toast.makeText(JoinMultiplayer.this, getString(R.string.LobbyNotFound), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
        if (!Stats.readPrivacy()) {
            btn_camera.setVisibility(View.VISIBLE);
        }
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photo = new File(getFilesDir(), "Photo.jpg");

                Utils.localPhotoUri = FileProvider.getUriForFile(JoinMultiplayer.this, "com.liadpaz.tic_tac_toe.fileprovider", photo);

                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Utils.localPhotoUri), 0);
            }
        });

        et_name_join.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameOk = s.length() > 0;
                btn_join.setEnabled(nameOk && numberOk && (photoOk || Stats.readPrivacy()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        et_lobby_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numberOk = s.length() > 0;
                btn_join.setEnabled(numberOk && nameOk && (photoOk || Stats.readPrivacy()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        btn_join.setEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK) {
            photoOk = true;
            btn_join.setEnabled(nameOk && numberOk);
        }
    }
}


