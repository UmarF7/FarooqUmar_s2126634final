package com.example.farooq_umar_s2126634;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class TrafficAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<TrafficSituation> situations;
    private LayoutInflater inflater;

    public TrafficAdapter(Context context, ArrayList<TrafficSituation> situations) {
        this.context = context;
        this.situations = situations;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return situations.size();
    }

    @Override
    public Object getItem(int position) {
        return situations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        RowHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.traffic_list_row, parent, false);
            holder = new RowHolder();
            holder.icon = view.findViewById(R.id.trafficRowIcon);
            holder.title = view.findViewById(R.id.trafficRowText);
            holder.label = view.findViewById(R.id.trafficRowLabel);
            view.setTag(holder);
        } else {
            holder = (RowHolder) view.getTag();
        }

        TrafficSituation item = situations.get(position);
        holder.icon.setImageResource(TrafficDisplay.getIcon(item));
        holder.icon.setContentDescription(TrafficDisplay.getLabel(item));
        holder.title.setText(item.type + " - " + item.comment);
        holder.label.setText(TrafficDisplay.getLabel(item));
        holder.label.setTextColor(context.getColor(TrafficDisplay.getLabelColor(item)));
        return view;
    }

    static class RowHolder {
        ImageView icon;
        TextView title;
        TextView label;
    }
}
