package com.c168.aaryahi;

import com.google.firebase.database.Exclude;

public class Task {
    @Exclude
    private String id;
    private String name;
    private String date;
    private String time;
    private float priority;
    private boolean completed;

    public Task() { }

    public Task(String name, String date, String time, float priority) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.priority = priority;
        this.completed = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public float getPriority() { return priority; }
    public void setPriority(float priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
