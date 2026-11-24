package com.example.farooq_umar_s2126634;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

public class TrafficDetailFragment extends Fragment implements View.OnClickListener {
    private BackListener listener;
    private Button mapButton;

    public interface BackListener {
        void onBackToList();
        void onMapRequested();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (BackListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traffic_detail, container, false);
        Button backButton = view.findViewById(R.id.backButton);
        mapButton = view.findViewById(R.id.mapButton);
        TextView details = view.findViewById(R.id.situationDetails);
        ImageView detailIcon = view.findViewById(R.id.detailIcon);
        TextView detailStatus = view.findViewById(R.id.detailStatus);
        backButton.setOnClickListener(this);
        mapButton.setOnClickListener(this);

        ViewModelProvider provider = new ViewModelProvider(getActivity());
        TrafficViewModel viewModel = provider.get(TrafficViewModel.class);
        TrafficSituation item = viewModel.getSelectedSituation();

        if (item != null) {
            detailIcon.setImageResource(TrafficDisplay.getIcon(item));
            detailIcon.setContentDescription(TrafficDisplay.getLabel(item));
            detailStatus.setText(TrafficDisplay.getLabel(item));
            detailStatus.setTextColor(getActivity().getColor(TrafficDisplay.getLabelColor(item)));
            String text = item.category + "\n\n";
            text += item.comment + "\n\n";
            text += "Type: " + item.type + "\n";
            if (!item.roadName.isEmpty()) {
                text += "Road: " + item.roadName + "\n";
            }
            if (!item.direction.isEmpty()) {
                text += "Direction: " + item.direction + "\n";
            }
            if (!item.detailType.isEmpty()) {
                text += "Event: " + item.detailType + "\n";
            }
            text += "Start: " + item.startTime + "\n";
            if (!item.endTime.isEmpty()) {
                text += "End: " + item.endTime + "\n";
            }
            if (!item.delay.isEmpty()) {
                text += "Delay: " + item.delay + "\n";
            }
            text += "\nTo: " + item.toLatitude + ", " + item.toLongitude;
            text += "\nFrom: " + item.fromLatitude + ", " + item.fromLongitude;
            details.setText(text);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view == mapButton) {
            listener.onMapRequested();
        } else {
            listener.onBackToList();
        }
    }
}
