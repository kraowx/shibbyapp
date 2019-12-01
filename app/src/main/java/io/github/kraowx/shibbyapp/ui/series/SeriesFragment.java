package io.github.kraowx.shibbyapp.ui.series;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.ui.allfiles.ShibbyFileArrayDialog;

public class SeriesFragment extends Fragment
        implements ShibbySeriesAdapter.ItemClickListener, SearchView.OnQueryTextListener
{
    private RecyclerView list;
    private ShibbySeriesAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.fragment_series, container, false);
        DataManager dataManager = new DataManager((MainActivity)getActivity());
        initializeList(root, dataManager.getSeries());

        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.hide();

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                SearchView searchView = ((MainActivity)getActivity()).getSearchView();
                if (searchView != null)
                {
                    searchView.setOnQueryTextListener(SeriesFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
        return root;
    }

    @Override
    public boolean onQueryTextChange(String text)
    {
        listAdapter.filterDisplayItems(text);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String text)
    {
        return false;
    }

    @Override
    public void onItemClick(View view, int position)
    {
        ShibbyFileArrayDialog dialog = new ShibbyFileArrayDialog(
                getContext(), listAdapter.getItem(position),
                (MainActivity)getActivity(), null);
    }

    private void initializeList(View root, final List<ShibbyFileArray> series)
    {
        list = root.findViewById(R.id.listSeries);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listLayoutManager = new LinearLayoutManager(getContext());
                list.setLayoutManager(listLayoutManager);
                listAdapter = new ShibbySeriesAdapter(getContext(), series);
                list.setAdapter(listAdapter);
                listAdapter.setClickListener(SeriesFragment.this);
            }
        });
    }
}