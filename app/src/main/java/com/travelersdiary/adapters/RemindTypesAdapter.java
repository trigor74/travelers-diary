package com.travelersdiary.adapters;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.travelersdiary.R;

public class RemindTypesAdapter extends ArrayAdapter<RemindTypesAdapter.RemindType> {

    public class RemindType {
        private String mTypeName;
        private int mTypeIconResourceId;

        public String getTypeName() {
            return mTypeName;
        }

        public void setTypeName(String typeName) {
            this.mTypeName = typeName;
        }

        public int getTypeIconResourceId() {
            return mTypeIconResourceId;
        }

        public void setTypeIconResourceId(int resourceId) {
            this.mTypeIconResourceId = resourceId;
        }
    }

    private Context mContext;
    private LayoutInflater mInflater;

    public RemindTypesAdapter(Context context) {
        super(context, R.layout.spinner_remind_type_item);

        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);

        String[] names = context.getResources().getStringArray(R.array.reminder_type_names);
        TypedArray icons = context.getResources().obtainTypedArray(R.array.reminder_type_icons);
        for (int i = 0; i < names.length; i++) {
            RemindType item = new RemindType();
            item.setTypeName(names[i]);
            item.setTypeIconResourceId(icons.getResourceId(i, -1));
            add(item);
        }
        icons.recycle();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(mInflater,
                position,
                convertView,
                parent,
                R.layout.spinner_remind_type_item,
                false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(mInflater,
                position,
                convertView,
                parent,
                R.layout.spinner_remind_type_item,
                true);
    }

    private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                        ViewGroup parent, int resource, boolean dropDown) {
        RemindType item = getItem(position);

        View view;
        TextView text;
        ImageView icon;

        if (convertView == null) {
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }

        text = (TextView) view.findViewById(R.id.spinner_remind_type_item_text);
        text.setText(item.getTypeName());
        if (dropDown) {
            text.setVisibility(View.VISIBLE);
        } else {
            text.setVisibility(View.GONE);
        }

        icon = (ImageView) view.findViewById(R.id.spinner_remind_type_item_icon);
        icon.setImageResource(item.getTypeIconResourceId());

        return view;
    }
}