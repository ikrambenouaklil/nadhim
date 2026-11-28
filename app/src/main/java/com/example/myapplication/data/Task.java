package com.example.myapplication.data;

import java.io.Serializable;

/** Data for one scheduled task. */
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    public long id;
    public String title;
    public String date;
    public int minutes;
    public int duration;
    public String notes;
    public String category;
    public String priority;
    public boolean done;

    public Task(long id, String title, String date, int minutes, int duration,
                String notes, String category, String priority, boolean done) {
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
}
