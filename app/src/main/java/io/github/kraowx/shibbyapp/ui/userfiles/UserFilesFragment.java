package io.github.kraowx.shibbyapp.ui.userfiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.ui.allfiles.ShibbyFileAdapter;
import io.github.kraowx.shibbyapp.ui.dialog.FileFilterController;
import io.github.kraowx.shibbyapp.ui.dialog.FileInfoDialog;

public class UserFilesFragment extends Fragment
        implements ShibbyFileAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener,
        FileFilterController.FilterListener
{
    private String[] fileTypes, tags;
    private int[] durations;
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private ShibbyFileAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;
    
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(
                R.layout.fragment_userfiles, container, false);
        final DataManager dataManager = new DataManager((MainActivity)getActivity());
    
        refreshLayout = (SwipeRefreshLayout)root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    
        new Thread()
        {
            @Override
            public void run()
            {
                initializeList(root, dataManager.getUserFiles());
            }
        }.start();
        
        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.hide();
    
        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                final SearchView searchView = ((MainActivity)getActivity()).getSearchView();
                if (searchView != null)
                {
                    searchView.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            searchView.setQuery("", false);
                            searchView.setIconified(true);
                        }
                    });
                    searchView.setOnQueryTextListener(UserFilesFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
    
        FileFilterController fileFilterController =
                ((MainActivity)getActivity()).getFileFilterController();
        fileFilterController.setButtonVisible(true);
        fileFilterController.setListener(this);
        return root;
    }
    
    @Override
    public void filtersUpdated(String[] fileTypes, int[] durations, String[] tags)
    {
        if (listAdapter != null)
        {
            this.fileTypes = fileTypes;
            this.durations = durations;
            this.tags = tags;
            listAdapter.filterDisplayItems(null, fileTypes, durations, tags);
        }
    }
    
    @Override
    public void onRefresh()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                updateList();
                refreshLayout.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        }.start();
    }
    
    @Override
    public boolean onQueryTextChange(String text)
    {
        if (listAdapter != null)
        {
            listAdapter.filterDisplayItems(text, this.fileTypes, this.durations, this.tags);
        }
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
        FileInfoDialog fileInfoDialog = new FileInfoDialog(
                (MainActivity)getActivity(), listAdapter.getItem(position),
                listAdapter.getData());
    }
    
    private void initializeList(View root, final List<ShibbyFile> files)
    {
        list = root.findViewById(R.id.listFiles);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listLayoutManager = new LinearLayoutManager(getContext());
                list.setLayoutManager(listLayoutManager);
                listAdapter = new ShibbyFileAdapter(getContext(),
                        files, ((MainActivity)getActivity()));
                list.setAdapter(listAdapter);
                listAdapter.setClickListener(UserFilesFragment.this);
            }
        });
    }
    
    private void updateList()
    {
        DataManager dataManager = new DataManager((MainActivity)getActivity());
        listAdapter.setData(dataManager.getUserFiles());
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
}
