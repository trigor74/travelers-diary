package com.travelersdiary;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class BindAdapters {

    @BindingConversion
    public static int convertBooleanToVisibility(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    @BindingAdapter({"adapter"})
    public static void bindAdapter(RecyclerView list, RecyclerView.Adapter adapter) {
        list.setAdapter(adapter);
    }

}
