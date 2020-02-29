package com.liadpaz.tic_tac_toe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.liadpaz.tic_tac_toe.databinding.LayoutLobbiesBinding;

import java.util.List;

// TODO: 29/2/2020 add view binding

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
        if (convertView == null) {
            convertView = binding.getRoot();
        }
        final TextView textView = binding.tvLobbyDev;
        ImageView delete = binding.ivDeleteLobby;

        textView.setText(lobbyTitle.get(position));
        delete.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setMessage(String.format("Are you sure you want to delete %s?", textView.getText()))
                .setTitle("Delete Lobby")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Firebase.dataRef.child("Lobbies").child(textView.getText().toString()).removeValue();
                    lobbyTitle.remove(position);
                    notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .show());

        return convertView;
    }
}
