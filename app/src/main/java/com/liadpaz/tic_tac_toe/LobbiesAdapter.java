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

import static com.liadpaz.tic_tac_toe.Constants.LOBBIES;

/**
 * This class is the adapter for the developer activity lobbies' list view.
 */
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
        delete.setOnClickListener(v -> new AlertDialog.Builder(context).setMessage(String.format("%s %s?", context.getString(R.string.delete_lobby_message), textView.getText())).setTitle(R.string.delete_lobby).setPositiveButton(R.string.yes, (dialog, which) -> {
            Firebase.dataRef.child(LOBBIES).child(textView.getText().toString()).removeValue();
            lobbyTitle.remove(position);
            notifyDataSetChanged();
        }).setNegativeButton(R.string.no, null).show());

        return convertView;
    }
}
