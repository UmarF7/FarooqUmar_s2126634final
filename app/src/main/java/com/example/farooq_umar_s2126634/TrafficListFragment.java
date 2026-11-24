package com.example.farooq_umar_s2126634;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class TrafficListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private SelectionListener listener;

    public interface SelectionListener {
        void onSituationSelected(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SelectionListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_traffic_list, container, false);
        TextView heading = view.findViewById(R.id.listHeading);
        ListView listView = view.findViewById(R.id.trafficList);

        ViewModelProvider provider = new ViewModelProvider(getActivity());
        TrafficViewModel viewModel = provider.get(TrafficViewModel.class);
        ArrayList<TrafficSituation> situations = viewModel.getVisibleSituations();
        if (viewModel.isSearchActive()) {
            heading.setText(viewModel.getSearchHeading() + " (" + situations.size() + ")");
        } else if (viewModel.isShowingRoadworks()) {
            heading.setText("Roadworks (" + situations.size() + ") - tap for details");
        } else {
            heading.setText("Incidents (" + situations.size() + ") - tap for details");
        }

        TrafficAdapter adapter = new TrafficAdapter(getActivity(), situations);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listener.onSituationSelected(position);
    }
}
