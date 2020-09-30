package com.example.abcapp.Notif.ui.carparkSelector;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.abcapp.Carpark;
import com.example.abcapp.Notif.CarparkAdapter;
import com.example.abcapp.Notif.NotifActivity;
import com.example.abcapp.R;

import java.util.ArrayList;

public class CarparkFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_location);
        View v = inflater.inflate(R.layout.fragment_carparkk, container, false);

        ArrayList<Carpark> list = new ArrayList<Carpark>();
        final CarparkAdapter adapter = new CarparkAdapter(getActivity(), list);
        ListView listView = v.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        list.add(new Carpark(0, "Carpark 1", 1.50, new double[]{1.00, 1.00}));
        list.add(new Carpark(1, "Carpark 2", 2.50, new double[]{1.10, 1.10}));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                NotifActivity.carpark = selected;
                Navigation.findNavController(v).navigate(R.id.action_navigation_location_to_navigation_home);
            }
        });

        SearchView searchView = v.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return v;
    }
}