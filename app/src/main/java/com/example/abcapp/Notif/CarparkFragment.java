package com.example.abcapp.Notif;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.abcapp.APICaller;
import com.example.abcapp.Carparks.Carpark;
import com.example.abcapp.Carparks.CarparkList;
import com.example.abcapp.Notif.CarparkAdapter;
import com.example.abcapp.Notif.Notification;
import com.example.abcapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CarparkFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_carpark, container, false);
        final Notification notification = (Notification) getArguments().getSerializable("notif");

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        APICaller apiCaller = new APICaller(requestQueue);
        ArrayList<Carpark> list = new ArrayList<>();
        try {
            // TODO: change to read carparks from the csv instead
//            list = apiCaller.getCarparkAddress();
            HashMap hashMap = CarparkList.getCarparks();
            list = new ArrayList<Carpark>(hashMap.values());

        } catch (Exception e) {
            e.printStackTrace();
        }

        final CarparkAdapter adapter = new CarparkAdapter(getActivity(), list);
        ListView listView = v.findViewById(R.id.listview);
        listView.setAdapter(adapter);

        final SearchView searchView = v.findViewById(R.id.search_view);
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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                searchView.clearFocus();
                Carpark selected = (Carpark) parent.getItemAtPosition(position);
                Bundle bundle = new Bundle();
                notification.setCarpark(selected);
                bundle.putSerializable("notif", notification);
                Navigation.findNavController(v).navigate(R.id.action_carpark_fragment_to_editFragment, bundle);
            }
        });

        return v;
    }
}
