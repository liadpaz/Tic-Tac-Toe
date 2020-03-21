package com.liadpaz.tic_tac_toe;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.UploadTask;
import com.liadpaz.tic_tac_toe.databinding.LayoutWaitDialogBinding;

import java.util.concurrent.TimeUnit;

import static com.liadpaz.tic_tac_toe.Constants.LOBBIES;

class WaitDialog extends Dialog {

    private static final String CLIENT_NAME = "clientName";
    private static final String HOST_MESSAGE = "hostMessage";
    private static final String CLIENT_MESSAGE = "clientMessage";
    private static final String CLIENT = "Client";
    private static final String HOST = "Host";
    private static final String UPLOADING = "uploading";
    private static final String UPLOAD = "upload";

    private Boolean isJoining = null;
    private boolean committed;

    private ValueEventListener valueEventListener;
    private DatabaseReference reference;
    private UploadTask task;
    private boolean isOtherSideReady = false;

    private String lobbyNumber;

    private Handler handler = new Handler();
    private Runnable callback;

    @SuppressWarnings("ConstantConditions")
    WaitDialog(Activity activity) {
        super(activity);
        LayoutWaitDialogBinding binding = LayoutWaitDialogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        handler.postDelayed(callback = () -> {
            committed = false;
            dismiss();
            Toast.makeText(activity, R.string.matchmaking_unavailable, Toast.LENGTH_LONG).show();
        }, TimeUnit.MINUTES.toMillis(2));

        binding.btnMatchmakingCancel.setOnClickListener(v -> {
            committed = false;
            dismiss();
        });

        Firebase.dataRef.child("Lobbies").runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                // looking for existing lobbies (join / client)
                if (currentData.hasChildren()) {
                    // iterating all the lobbies found
                    for (MutableData lobby : currentData.getChildren()) {
                        // checking if lobby is available for matchmaking and is vacant
                        if (lobby.getValue(Lobby.class).isMatchmaking() && !currentData.hasChild(CLIENT_NAME)) {
                            isJoining = true;
                            lobbyNumber = lobby.getKey();
                            lobby.child(CLIENT_NAME).setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                            lobby.child(CLIENT_MESSAGE).setValue(UPLOADING);
                            (task = Firebase.storeRef.child("Lobbies").child(lobbyNumber).child(CLIENT).putFile(Utils.localPhotoUri)).addOnCompleteListener(task -> {
                                Firebase.dataRef.child("Lobbies").child(lobbyNumber).child(CLIENT_MESSAGE).setValue(UPLOAD);
                                activity.runOnUiThread(() -> binding.tvWaitingMatchmaking.setText(R.string.matchmaking_waiting_other_player));
                                if (isOtherSideReady) {
                                    dismiss();
                                    activity.startActivity(new Intent(activity, GameActivity.class).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.CLIENT).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.STARTING_EXTRA, Cell.Type.X));
                                }
                            });
                            activity.runOnUiThread(() -> binding.tvWaitingMatchmaking.setText(R.string.matchmaking_uploading_photo));
                            return Transaction.success(currentData);
                        }
                    }
                }
                // creating new lobby (host)
                for (String number : Utils.getRoomNumber()) {
                    if (!currentData.hasChild(number)) {
                        isJoining = false;
                        lobbyNumber = number;
                        currentData.child(number).setValue(new Lobby(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), true, 3, false, "X", 0));
                        currentData.child(number).child(HOST_MESSAGE).setValue(UPLOADING);
                        (task = Firebase.storeRef.child(LOBBIES).child(lobbyNumber).child(HOST).putFile(Utils.localPhotoUri)).addOnCompleteListener(task -> {
                            Firebase.dataRef.child(LOBBIES).child(lobbyNumber).child(HOST_MESSAGE).setValue(UPLOAD);
                            if (isOtherSideReady) {
                                dismiss();
                                activity.startActivity(new Intent(activity, GameActivity.class).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.CLIENT).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.STARTING_EXTRA, Cell.Type.X));
                            }
                        });
                        return Transaction.success(currentData);
                    }
                }
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                WaitDialog.this.committed = committed;
                if (committed && isJoining != null) {
                    valueEventListener = (reference = currentData.getRef().child(lobbyNumber)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChildren()) {
                                WaitDialog.this.committed = false;
                                dismiss();
                                return;
                            }
                            if (isJoining) {    // joining / client
                                if (UPLOAD.equals(dataSnapshot.child(HOST_MESSAGE).getValue(String.class))) {
                                    isOtherSideReady = true;
                                    WaitDialog.this.committed = true;
                                    if (task.isComplete()) {
                                        dismiss();
                                        activity.startActivity(new Intent(activity, GameActivity.class).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.CLIENT).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.STARTING_EXTRA, Cell.Type.X).putExtra(Constants.MAX_EXTRA, 3));
                                        reference.removeEventListener(valueEventListener);
                                    } else {
                                        activity.runOnUiThread(() -> binding.tvWaitingMatchmaking.setText(R.string.matchmaking_uploading_photo));
                                    }
                                }
                            } else {    // host
                                if (UPLOAD.equals(dataSnapshot.child(CLIENT_MESSAGE).getValue(String.class))) {
                                    activity.runOnUiThread(() -> binding.tvWaitingMatchmaking.setText(R.string.matchmaking_waiting_other_player));
                                    isOtherSideReady = true;
                                    WaitDialog.this.committed = true;
                                    if (task.isComplete()) {
                                        dismiss();
                                        activity.startActivity(new Intent(activity, GameActivity.class).putExtra(Constants.MULTIPLAYER_EXTRA, Constants.HOST).putExtra(Constants.LOBBY_NUMBER_EXTRA, lobbyNumber).putExtra(Constants.STARTING_EXTRA, Cell.Type.X).putExtra(Constants.MAX_EXTRA, 3));
                                        reference.removeEventListener(valueEventListener);
                                    }
                                } else if (dataSnapshot.hasChild(CLIENT_NAME)) {
                                    handler.removeCallbacks(callback);
                                    activity.runOnUiThread(() -> binding.tvWaitingMatchmaking.setText(R.string.matchmaking_waiting_other_player));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                    reference.onDisconnect().removeValue();
                }
            }
        });

        setOnDismissListener(dialog -> {
            if (valueEventListener != null) {
                this.reference.removeEventListener(valueEventListener);
            }
            if (!committed && this.reference != null) {
                this.reference.removeValue();
            }
        });
    }
}
