package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.tic_tac_toe.databinding.LayoutLobbiesBinding;

import java.util.List;

public class LobbiesAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final List<String> lobbyTitle;

    LobbiesAdapter(@NonNull Activity context, List<String> lobbyTitle) {
        super(context, R.layout.layout_lobbies, lobbyTitle);
        this.context = context;
        this.lobbyTitle = lobbyTitle;
    }

    @NonNull
    @Override
    @SuppressLint({"ViewHolder", "InflateParams"})
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutLobbiesBinding binding = LayoutLobbiesBinding.inflate(context.getLayoutInflater(), parent, false);

        final TextView tv_lobby_dev = binding.tvLobbyDev;
        binding.ivDeleteLobby.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setMessage(String.format("Are you sure you want to delete %s?", tv_lobby_dev.getText()))
                .setTitle("Delete Lobby")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Firebase.dataRef.child("Lobbies").child(tv_lobby_dev.getText().toString()).removeValue();
                    lobbyTitle.remove(position);
                    notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .show());
        tv_lobby_dev.setText(lobbyTitle.get(position));
        binding.getRoot().setOnLongClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setMessage(String.format("Are you sure you want to delete %s?", binding.tvLobbyDev.getText()))
                    .setTitle("Delete Lobby")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Firebase.dataRef.child("Lobbies").child(binding.tvLobbyDev.getText().toString()).removeValue();
                        lobbyTitle.remove(position);
                        notifyDataSetChanged();
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        return binding.getRoot();
    }
}
