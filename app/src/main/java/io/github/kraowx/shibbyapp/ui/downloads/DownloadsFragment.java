package io.github.kraowx.shibbyapp.ui.downloads;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

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
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;

public class DownloadsFragment extends Fragment
        implements ShibbyFileAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener
{
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private ShibbyFileAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.fragment_downloads, container, false);
        final DataManager dataManager = new DataManager((MainActivity)getActivity());
        new Thread()
        {
            @Override
            public void run()
            {
                List<ShibbyFile> allFiles = dataManager.getFiles();
                List<ShibbyFile> downloadedFiles = AudioDownloadManager
                        .getDownloadedFiles((MainActivity) getActivity(), allFiles);
                initializeList(root, downloadedFiles);
            }
        }.start();

        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.hide();

        refreshLayout = (SwipeRefreshLayout)root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        refreshLayout.setRefreshing(true);

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                SearchView searchView = ((MainActivity)getActivity()).getSearchView();
                if (searchView != null)
                {
                    searchView.setOnQueryTextListener(DownloadsFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
        return root;
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
        createFileInfoDialog(listAdapter.getItem(position));
    }

    private void initializeList(View root, final List<ShibbyFile> files)
    {
        list = root.findViewById(R.id.listDownloads);
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
                listAdapter.setClickListener(DownloadsFragment.this);
                refreshLayout.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void updateList()
    {
        List<ShibbyFile> allFiles = new DataManager(
                (MainActivity)getActivity()).getFiles();
        List<ShibbyFile> downloadedFiles = AudioDownloadManager
                .getDownloadedFiles((MainActivity)
                        getActivity(), allFiles);
        listAdapter.setData(downloadedFiles);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createFileInfoDialog(ShibbyFile file)
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.file_info_dialog);
        dialog.setTitle("File Info");
        TextView title = dialog.findViewById(R.id.txtTitle);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences((MainActivity)getActivity());
        boolean displayLongNames = prefs.getBoolean(
                "displayLongNames", false);
        title.setText(displayLongNames ?
                file.getName() : file.getShortName());
        TextView tags = dialog.findViewById(R.id.txtTags);
        tags.setText(getTagsString(file.getTags()));
        TextView description = dialog.findViewById(R.id.txtDescription);
        description.setText(file.getDescription());
        dialog.show();
    }

    private String getTagsString(List<String> tags)
    {
        String tagsStr = "";
        for (int i = 0; i < tags.size(); i++)
        {
            tagsStr += tags.get(i);
            if (i < tags.size()-1)
            {
                tagsStr += "  |  ";
            }
        }
        return tagsStr + "\n";
    }
}