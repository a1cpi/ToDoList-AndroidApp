package com.c168.aaryahi;

import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnStartDragListener {

    private static final String DB_URL =
            "https://mad-assignment-2-97743-default-rtdb.asia-southeast1.firebasedatabase.app";

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;
    private Button createNewTaskButton;
    private Button viewCompletedButton;
    private TextView emptyTextView;
    private DatabaseReference mDatabase;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        createNewTaskButton = findViewById(R.id.button);
        viewCompletedButton = findViewById(R.id.button_completed);
        emptyTextView = findViewById(R.id.empty_text_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        // Pass 'false' for isCompletedScreen
        taskAdapter = new TaskAdapter(this, taskList, this, false);
        recyclerView.setAdapter(taskAdapter);

        FirebaseDatabase database = FirebaseDatabase.getInstance(DB_URL);
        mDatabase = database.getReference("tasks");

        createNewTaskButton.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, SecondActivity.class))
        );

        viewCompletedButton.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ThirdActivity.class))
        );

        setupItemTouchHelper();
        fetchTasks();
    }

    private void fetchTasks() {
        mDatabase.orderByChild("priority").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                taskList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Task task = snapshot.getValue(Task.class);
                    if (task != null) {
                        task.setId(snapshot.getKey());
                        if (!task.isCompleted()) {
                            taskList.add(task);
                        }
                    }
                }
                Collections.reverse(taskList);
                taskAdapter.notifyDataSetChanged();
                checkEmptyState();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error
            }
        });
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean onMove(@NonNull RecyclerView rv,
                                  @NonNull RecyclerView.ViewHolder from,
                                  @NonNull RecyclerView.ViewHolder to) {
                taskAdapter.onItemMove(from.getAdapterPosition(), to.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }

            @Override
            public void clearView(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(rv, viewHolder);
                updateTaskPriorities();
            }
        };
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog(int position) {
        if (position < 0 || position >= taskList.size()) return;
        Task taskToDelete = taskList.get(position);
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete task \"" + taskToDelete.getName() + "\"?")
                .setPositiveButton("Yes", (dialog, which) ->
                        mDatabase.child(taskToDelete.getId()).removeValue())
                .setNegativeButton("No", (dialog, which) ->
                        taskAdapter.notifyItemChanged(position))
                .setOnCancelListener(dialog ->
                        taskAdapter.notifyItemChanged(position))
                .show();
    }

    private void updateTaskPriorities() {
        if (taskList.isEmpty()) return;
        Map<String, Object> updates = new HashMap<>();
        int n = taskList.size();
        if (n == 1) {
            Task t = taskList.get(0);
            if (t.getId() != null) {
                t.setPriority(1f);
                updates.put(t.getId() + "/priority", 1f);
            }
        } else {
            for (int i = 0; i < n; i++) {
                Task t = taskList.get(i);
                if (t.getId() != null) {
                    float newPriority = 1.0f - (i / (float) (n - 1));
                    t.setPriority(newPriority);
                    updates.put(t.getId() + "/priority", newPriority);
                }
            }
        }
        if (!updates.isEmpty()) {
            mDatabase.updateChildren(updates);
        }
    }

    private void checkEmptyState() {
        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (itemTouchHelper != null) {
            itemTouchHelper.startDrag(viewHolder);
        }
    }
}
