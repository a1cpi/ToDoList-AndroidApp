package com.c168.aaryahi;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ThirdActivity extends AppCompatActivity {

    private static final String DB_URL =
            "https://mad-assignment-2-97743-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView recyclerView;
    private TextView emptyText;
    private Button backButton;
    private TaskAdapter adapter;
    private final List<Task> completedTasks = new ArrayList<>();
    private DatabaseReference tasksRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        recyclerView = findViewById(R.id.recycler_completed);
        emptyText = findViewById(R.id.empty_completed);
        backButton = findViewById(R.id.button_back);

        backButton.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Pass 'true' for isCompletedScreen
        adapter = new TaskAdapter(this, completedTasks, null, true);
        recyclerView.setAdapter(adapter);

        tasksRef = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");

        attachSwipeToDelete();
        fetchCompleted();
    }

    private void fetchCompleted() {
        tasksRef.orderByChild("priority").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                completedTasks.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task t = child.getValue(Task.class);
                    if (t != null) {
                        t.setId(child.getKey());
                        if (t.isCompleted()) {
                            completedTasks.add(t);
                        }
                    }
                }
                Collections.reverse(completedTasks);
                adapter.notifyDataSetChanged();
                checkEmpty();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Log error
            }
        });
    }

    private void attachSwipeToDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder a, @NonNull RecyclerView.ViewHolder b) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getAdapterPosition();
                if (pos < 0 || pos >= completedTasks.size()) return;
                Task task = completedTasks.get(pos);
                new AlertDialog.Builder(ThirdActivity.this)
                        .setTitle("Delete Task")
                        .setMessage("Permanently delete \"" + task.getName() + "\"?")
                        .setPositiveButton("Yes", (d, w) -> tasksRef.child(task.getId()).removeValue())
                        .setNegativeButton("No", (d, w) -> adapter.notifyItemChanged(pos))
                        .setOnCancelListener(d -> adapter.notifyItemChanged(pos))
                        .show();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void checkEmpty() {
        if (completedTasks.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}
