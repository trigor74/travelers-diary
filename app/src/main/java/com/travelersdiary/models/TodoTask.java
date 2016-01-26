package com.travelersdiary.models;

public class TodoTask {
    private String item;
    private boolean checked;

    public TodoTask() {
    }

    public TodoTask(String item, boolean checked) {
        this.item = item;
        this.checked = checked;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
