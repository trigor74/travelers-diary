package com.travelersdiary.interfaces;

import android.view.View;

public interface IOnItemClickListener {
    void onItemClick(View view, int position);

    void onItemLongClick(View view, int position);
}
