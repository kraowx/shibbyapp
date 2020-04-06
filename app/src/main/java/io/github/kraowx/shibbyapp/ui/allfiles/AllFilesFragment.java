package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.RequestType;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.HttpRequest;
import io.github.kraowx.shibbyapp.ui.dialog.FileFilterController;
import io.github.kraowx.shibbyapp.ui.dialog.FileInfoDialog;
import io.github.kraowx.shibbyapp.ui.dialog.ServerSelectorDialog;
import io.github.kraowx.shibbyapp.ui.playlists.AddFileToPlaylistDialog;

public class AllFilesFragment extends Fragment
        implements ShibbyFileAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener,
        ServerSelectorDialog.ServerSelectedListener, FileFilterController.FilterListener
{
    private String[] fileTypes, tags;
    private int[] durations;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private View root;
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private ShibbyFileAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        root = inflater.inflate(R.layout.fragment_allfiles, container, false);

        refreshLayout = (SwipeRefreshLayout)root.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    
        prefs = PreferenceManager.getDefaultSharedPreferences(
                (MainActivity)getActivity());
        editor = prefs.edit();
        startInitialUpdate();

        FloatingActionButton fabAdd = ((MainActivity)getActivity())
                .findViewById(R.id.fabAddPlaylist);
        fabAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddFileToPlaylistDialog dialog = new AddFileToPlaylistDialog(
                        (MainActivity)getActivity(), listAdapter.getCheckedFiles()
                        .toArray(new ShibbyFile[0]), false);
                dialog.setFilesAddedListener(new AddFileToPlaylistDialog.FilesAddedListener()
                {
                    @Override
                    public void filesAdded(boolean added)
                    {
                        if (added)
                        {
                            listAdapter.clearCheckedFiles();
                            listAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
        fabAdd.hide();

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                final SearchView searchView =
                        ((MainActivity)getActivity()).getSearchView();
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
                    searchView.setOnQueryTextListener(AllFilesFragment.this);
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
    public void onServerSelected()
    {
        startInitialUpdate();
    }

    @Override
    public void onRefresh()
    {
        if (listAdapter != null)
        {
            new Thread()
            {
                @Override
                public void run()
                {
                    new DataManager((MainActivity) getActivity())
                            .requestData(Request.files());
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
        else
        {
            refreshLayout.post(new Runnable()
            {
                @Override
                public void run()
                {
                    refreshLayout.setRefreshing(false);
                }
            });
            Toast.makeText((MainActivity)getActivity(),
                    "No server specified", Toast.LENGTH_LONG).show();
        }
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
    
    private void startInitialUpdate()
    {
        final ProgressDialog progressDialog = new ProgressDialog((MainActivity)getActivity());
        new Thread()
        {
            @Override
            public void run()
            {
                refreshLayout.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        refreshLayout.setRefreshing(true);
                    }
                });
                final DataManager dataManager = new DataManager((MainActivity) getActivity());
                final List<ShibbyFile> files = dataManager.getFiles();
                if (files.size() > 0)
                {
                    boolean updateStartup = prefs.getBoolean(
                            "updateStartup", true);
                    boolean isStartup = prefs.getBoolean("isStartup", true);
                    if (updateStartup && isStartup)
                    {
                        editor.putBoolean("isStartup", false);
                        editor.commit();
                        refreshLayout.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                refreshLayout.setRefreshing(true);
                            }
                        });
                        new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    dataManager.requestData(Request.all());
                                }
                                catch (HttpRequest.HttpRequestException hre)
                                {
                                    hre.printStackTrace();
                                }
                                ((MainActivity) getActivity()).runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        initializeList(root, dataManager.getFiles());
                                        refreshLayout.setRefreshing(false);
                                    }
                                });
                            }
                        }.start();
                    }
                    else
                    {
                        initializeList(root, files);
                        refreshLayout.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                refreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
                else
                {
                    ((MainActivity)getActivity()).runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            progressDialog.setTitle("Fetching first-time data...");
                            progressDialog.show();
                        }
                    });
                    new Timer().scheduleAtFixedRate(new TimerTask()
                    {
                        @Override
                        public void run()
                        {
                            List<ShibbyFile> files = dataManager.getFiles();
                            if (files.size() > 0)
                            {
                                initializeList(root, files);
                                ((MainActivity) getActivity()).runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        if (progressDialog.isShowing())
                                        {
                                            progressDialog.hide();
                                        }
                                        refreshLayout.setRefreshing(false);
                                    }
                                });
                                this.cancel();
                            }
                            else
                            {
                                dataManager.requestData(getAllRequest());
                            }
                        }
                    }, 0, 5000);
                }
            }
        }.start();
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
                listAdapter.setClickListener(AllFilesFragment.this);
            }
        });
    }

    private void updateList()
    {
        DataManager dataManager = new DataManager((MainActivity)getActivity());
        listAdapter.setData(dataManager.getFiles());
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private Request getAllRequest()
    {
        if (((MainActivity)getActivity()).getPatreonSessionManager().isAuthenticated())
        {
            try
            {
                SharedPreferences prefs = PreferenceManager
                        .getDefaultSharedPreferences((MainActivity)getActivity());
                JSONObject data = new JSONObject();
                data.put("email", prefs.getString("patreonEmail", null));
                data.put("password", prefs.getString("patreonPassword", null));
                return new Request(RequestType.ALL, data);
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return Request.all();
    }
}