package com.c168.aaryahi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    private static final String DB_URL =
            "https://mad-assignment-2-97743-default-rtdb.asia-southeast1.firebasedatabase.app";

    private final List<Task> taskList;
    private final Context context;
    private final OnStartDragListener dragStartListener;
    private final boolean isCompletedScreen;
    private final DatabaseReference tasksRef;

    public TaskAdapter(Context context, List<Task> taskList, OnStartDragListener dragStartListener, boolean isCompletedScreen) {
        this.context = context;
        this.taskList = taskList;
        this.dragStartListener = dragStartListener;
        this.isCompletedScreen = isCompletedScreen;
        this.tasksRef = FirebaseDatabase.getInstance(DB_URL).getReference("tasks");
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task currentTask = taskList.get(position);

        holder.taskDescription.setText(currentTask.getName());
        holder.taskDateTime.setText(String.format("%s at %s", currentTask.getDate(), currentTask.getTime()));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(currentTask.isCompleted());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentTask.getId() != null) {
                tasksRef.child(currentTask.getId()).child("completed").setValue(isChecked);
            }
        });

        holder.editIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, SecondActivity.class);
            intent.putExtra("TASK_ID", currentTask.getId());
            intent.putExtra("TASK_NAME", currentTask.getName());
            intent.putExtra("TASK_DATE", currentTask.getDate());
            intent.putExtra("TASK_TIME", currentTask.getTime());
            intent.putExtra("TASK_PRIORITY", currentTask.getPriority());
            context.startActivity(intent);
        });

        if (dragStartListener != null) {
            holder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder);
                }
                return false;
            });
        } else {
            holder.dragHandle.setVisibility(View.GONE);
        }

        // Conditionally check the deadline
        if (isCompletedScreen) {
            holder.taskDeadlineWarning.setVisibility(View.GONE);
        } else {
            checkDeadline(holder, currentTask);
        }
    }

    private void checkDeadline(TaskViewHolder holder, Task task) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        try {
            Date taskDateTime = sdf.parse(task.getDate() + " " + task.getTime());
            if (taskDateTime != null && taskDateTime.before(Calendar.getInstance().getTime())) {
                holder.taskDeadlineWarning.setVisibility(View.VISIBLE);
            } else {
                holder.taskDeadlineWarning.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            holder.taskDeadlineWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(taskList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(taskList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView taskDescription, taskDateTime, taskDeadlineWarning;
        ImageView editIcon, dragHandle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.task_checkbox);
            taskDescription = itemView.findViewById(R.id.task_description);
            taskDateTime = itemView.findViewById(R.id.task_datetime);
            taskDeadlineWarning = itemView.findViewById(R.id.task_deadline_warning);
            editIcon = itemView.findViewById(R.id.edit_task_icon);
            dragHandle = itemView.findViewById(R.id.drag_handle);
        }
    }
}
