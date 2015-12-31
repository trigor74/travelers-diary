package com.travelersdiary.models;

import java.util.ArrayList;

/**
 * Created by itrifonov on 30.12.2015.
 */
public class TodoItem {
    private String title;
    private ArrayList<TodoTask> task;
    private Boolean viewAsCheckboxes;
    private Boolean completed;
    // TODO: 30.12.2015 add remind at time and in a place

    public TodoItem() {
    }

    public TodoItem(String title, ArrayList<TodoTask> task, Boolean viewAsCheckboxes, Boolean completed) {
        this.title = title;
        this.task = task;
        this.viewAsCheckboxes = viewAsCheckboxes;
        this.completed = completed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<TodoTask> getTask() {
        return task;
    }

    public void setTask(ArrayList<TodoTask> task) {
        this.task = task;
    }

    public Boolean getViewAsCheckboxes() {
        return viewAsCheckboxes;
    }

    public void setViewAsCheckboxes(Boolean viewAsCheckboxes) {
        this.viewAsCheckboxes = viewAsCheckboxes;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}
