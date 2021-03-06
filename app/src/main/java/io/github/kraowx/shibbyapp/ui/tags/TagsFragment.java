package io.github.kraowx.shibbyapp.ui.tags;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.net.Request;
import io.github.kraowx.shibbyapp.net.RequestType;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.ui.allfiles.ShibbyFileArrayDialog;
import io.github.kraowx.shibbyapp.ui.dialog.FileFilterController;

public class TagsFragment extends Fragment
        implements ShibbyTagAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener
{
    private RecyclerView list;
    private SwipeRefreshLayout refreshLayout;
    private ShibbyTagAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        final View root = inflater.inflate(R.layout.fragment_tags, container, false);
        final DataManager dataManager = new DataManager((MainActivity)getActivity());
        new Thread()
        {
            @Override
            public void run()
            {
                initializeList(root, dataManager.getTags());
            }
        }.start();


        FloatingActionButton fabAdd =
                ((MainActivity)getActivity()).findViewById(R.id.fabAdd);
        fabAdd.setImageResource(R.drawable.ic_add);
        fabAdd.hide();

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
                    searchView.setOnQueryTextListener(TagsFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
    
        FileFilterController fileFilterController =
                ((MainActivity)getActivity()).getFileFilterController();
        fileFilterController.setButtonVisible(false);
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
                new DataManager((MainActivity)getActivity())
                        .requestData(getTagsRequest());
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
            listAdapter.filterDisplayItems(text);
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
        ShibbyFileArrayDialog dialog = new ShibbyFileArrayDialog(
                getContext(), listAdapter.getItem(position),
                (MainActivity)getActivity(), null);
    }

    private void initializeList(View root, final List<ShibbyFileArray> tags)
    {
        list = root.findViewById(R.id.listTags);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listLayoutManager = new LinearLayoutManager(getContext());
                list.setLayoutManager(listLayoutManager);
                listAdapter = new ShibbyTagAdapter(getContext(), tags);
                list.setAdapter(listAdapter);
                listAdapter.setClickListener(TagsFragment.this);
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
        DataManager dataManager = new DataManager((MainActivity)getActivity());
        listAdapter.setData(dataManager.getTags());
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                listAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private Request getTagsRequest()
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
                return new Request(RequestType.TAGS, data);
            }
            catch (JSONException je)
            {
                je.printStackTrace();
            }
        }
        return Request.tags();
    }
}