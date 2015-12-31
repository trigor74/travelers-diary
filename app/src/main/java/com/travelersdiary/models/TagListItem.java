package com.travelersdiary.models;

/**
 * Created by itrifonov on 31.12.2015.
 */
public class TagListItem {
    private String text;
    private int count;

    public TagListItem() {
    }

    public TagListItem(String text, int count) {
        this.text = text;
        this.count = count;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
