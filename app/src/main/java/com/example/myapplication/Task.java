package com.example.myapplication;


public class Task {
    public long id;
    public String title;
    public String date;
    public int minutes;
    public int duration;
    public String notes;
    public String category;
    public String priority;
    public boolean done;

    public Task(long id, String title, String date, int minutes, int duration, String notes, String category, String priority, boolean done) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.minutes = minutes;
        this.duration = duration;
        this.notes = notes;
        this.category = category;
        this.priority = priority;
        this.done = done;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getDuration() {
        return duration;
    }

    public String getNotes() {
        return notes;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isDone() {
        return done;
    }


    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}