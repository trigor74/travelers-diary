package com.travelersdiary.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseRecyclerAdapter;

public class FirebaseContextMenuRecyclerView extends RecyclerView {

    private FirebaseRecyclerViewContextMenuInfo mContextMenuInfo;

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

    @Override
    public boolean showContextMenuForChild(View originalView) {
        final int position = getChildAdapterPosition(originalView);
        if (position >= 0) {
            final long id = getAdapter().getItemId(position);
            final Firebase ref = ((FirebaseRecyclerAdapter) getAdapter()).getRef(position);
            mContextMenuInfo = new FirebaseRecyclerViewContextMenuInfo(position, id, originalView, ref);
            return super.showContextMenuForChild(originalView);
        }
        return false;
    }

    public FirebaseContextMenuRecyclerView(Context context) {
        super(context);
    }

    public FirebaseContextMenuRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FirebaseContextMenuRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static class FirebaseRecyclerViewContextMenuInfo implements ContextMenu.ContextMenuInfo {

        public FirebaseRecyclerViewContextMenuInfo(int position, long id, View targetView, Firebase ref) {
            this.position = position;
            this.id = id;
            this.targetView = targetView;
            this.ref = ref;
        }

        final public int position;
        final public long id;
        final public View targetView;
        final public Firebase ref;
    }
}
