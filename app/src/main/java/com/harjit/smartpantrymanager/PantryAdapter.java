package com.harjit.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class PantryAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final ArrayList<PantryItem> items = new ArrayList<>();
    private final DecimalFormat quantityFormat = new DecimalFormat("0.###");

    public PantryAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    public void updateItems(ArrayList<PantryItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public PantryItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // Reuse existing rows when scrolling.
        if (convertView == null) {
            convertView = inflater.inflate(
                    android.R.layout.simple_list_item_2, parent, false);

            holder = new ViewHolder();
            holder.name = convertView.findViewById(android.R.id.text1);
            holder.details = convertView.findViewById(android.R.id.text2);

            holder.name.setTextSize(19);
            holder.name.setTextColor(0xFF234B35);
            holder.details.setTextSize(15);
            holder.details.setTextColor(0xFF454545);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PantryItem item = getItem(position);

        holder.name.setText(item.getName());
        holder.details.setText(
                quantityFormat.format(item.getQuantity())
                        + " " + item.getUnit()
                        + " • Tap to edit or delete"
        );

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView details;
    }
}