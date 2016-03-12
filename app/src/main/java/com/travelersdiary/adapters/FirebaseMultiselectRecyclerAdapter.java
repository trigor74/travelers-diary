package com.travelersdiary.adapters;

import android.util.SparseBooleanArray;
import android.view.View;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.ui.FirebaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseMultiselectRecyclerAdapter<T, VH extends android.support.v7.widget.RecyclerView.ViewHolder> extends FirebaseRecyclerAdapter<T, VH> {
    public FirebaseMultiselectRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, Query ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public FirebaseMultiselectRecyclerAdapter(Class<T> modelClass, int modelLayout, Class<VH> viewHolderClass, Firebase ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private static OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClick(v, getLayoutPosition());
                }
            });
            itemView.setLongClickable(true);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemLongClick(v, getLayoutPosition());
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        if (isSelectable()) {
            viewHolder.itemView.setActivated(isSelected(position));
        } else {
            viewHolder.itemView.setActivated(false);
        }
    }

    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();
    private HashMap<Integer, Firebase> mSelectedItemsRef = new HashMap<>();
    private boolean mSelectable;

    public void setSelectable(boolean selectable) {
        mSelectable = selectable;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setSelected(int position, boolean checked) {
        if (checked) {
            mSelectedItems.put(position, true);
            mSelectedItemsRef.put(position, getRef(position));
        } else {
            mSelectedItems.delete(position);
            mSelectedItemsRef.remove(position);
        }
        notifyItemChanged(position);
    }

    public boolean tapSelection(int position) {
        if (isSelectable()) {
            if (!mSelectedItems.get(position, false)) {
                mSelectedItems.put(position, true);
                mSelectedItemsRef.put(position, getRef(position));
            } else {
                mSelectedItems.delete(position);
                mSelectedItemsRef.remove(position);
            }
            notifyItemChanged(position);
            return true;
        } else {
            return false;
        }
    }

    public boolean isSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clearSelections() {
        mSelectedItems.clear();
        mSelectedItemsRef.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return mSelectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(mSelectedItems.size());
        for (int i = 0; i < mSelectedItems.size(); i++) {
            if (mSelectedItems.valueAt(i)) {
                items.add(mSelectedItems.keyAt(i));
            }
        }
        return items;
    }

    public List<Firebase> getSelectedItemsRef() {
        List<Firebase> items = new ArrayList<>(mSelectedItemsRef.size());
        items.addAll(mSelectedItemsRef.values());
        return items;
    }
}
