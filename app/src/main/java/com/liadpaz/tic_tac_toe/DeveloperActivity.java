package com.liadpaz.tic_tac_toe;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.liadpaz.tic_tac_toe.databinding.ActivityDeveloperBinding;

import java.util.ArrayList;

import static com.liadpaz.tic_tac_toe.Constants.LOBBIES;

/**
 * This activity is the developer activity, it is to close any lobbies that somehow still open.
 */
public class DeveloperActivity extends AppCompatActivity {

    private ArrayList<String> lobbiesNumber;

    private ListView lv_lobbies;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityDeveloperBinding binding = ActivityDeveloperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbarDeveloper);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lobbiesNumber = new ArrayList<>();

        lv_lobbies = binding.lvLobbies;
        lv_lobbies.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(DeveloperActivity.this).setMessage(String.format("%s %s?", getString(R.string.delete_lobby_message), ((TextView)((ConstraintLayout)view).getViewById(R.id.tv_lobby_dev)).getText())).setTitle(R.string.delete_lobby).setPositiveButton(R.string.yes, (dialog, which) -> {
                Firebase.dataRef.child(LOBBIES).child(((TextView)((ConstraintLayout)view).getViewById(R.id.tv_lobby_dev)).getText().toString()).removeValue();
                getLobbies();
            }).setNegativeButton(R.string.no, null).show();
            return true;
        });

        getLobbies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dev, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        new AlertDialog.Builder(DeveloperActivity.this).setTitle(R.string.delete_all).setMessage(R.string.delete_all_message).setPositiveButton(R.string.yes, (dialog, which) -> {
            for (String lobby : lobbiesNumber) {
                Firebase.dataRef.child(LOBBIES).child(lobby).removeValue();
            }
            getLobbies();
        }).setNegativeButton(R.string.no, null).show();
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function downloads all the lobbies to the {@code lobbiesNumber} variable and displays it
     * on screen as a list
     */
    private void getLobbies() {
        Firebase.dataRef.child(LOBBIES).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lobbiesNumber.clear();
                for (DataSnapshot lobby : dataSnapshot.getChildren()) {
                    lobbiesNumber.add(lobby.getKey());
                }
                final LobbiesAdapter adapter = new LobbiesAdapter(DeveloperActivity.this, lobbiesNumber);
                lv_lobbies.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }
}
