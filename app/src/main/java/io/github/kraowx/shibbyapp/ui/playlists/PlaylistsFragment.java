package io.github.kraowx.shibbyapp.ui.playlists;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;
import io.github.kraowx.shibbyapp.ui.allfiles.ShibbyFileArrayDialog;
import io.github.kraowx.shibbyapp.ui.playlists.itemtouch.OnStartDragListener;
import io.github.kraowx.shibbyapp.ui.playlists.itemtouch.SimpleItemTouchHelperCallback;

public class PlaylistsFragment extends Fragment
        implements ShibbyPlaylistAdapter.ItemClickListener,
        SearchView.OnQueryTextListener, OnStartDragListener
{
    private RecyclerView list;
    private ShibbyPlaylistAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;
    private ItemTouchHelper mItemTouchHelper;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_playlists, container, false);

        List<String> playlists = PlaylistManager.getPlaylists(getContext());
        initializeList(root, playlists);

        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showCreatePlaylistDialog();
            }
        });
        fabAddPlaylist.show();

        new Timer().scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                SearchView searchView = ((MainActivity)getActivity()).getSearchView();
                if (searchView != null)
                {
                    searchView.setOnQueryTextListener(PlaylistsFragment.this);
                    this.cancel();
                }
            }
        }, 0, 1000);
        return root;
    }
    
    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder)
    {
        mItemTouchHelper.startDrag(viewHolder);
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
        String playlistName = listAdapter.getItem(position);
        List<ShibbyFile> files = PlaylistManager.getFilesFromPlaylist(
                (MainActivity)getActivity(), playlistName);
        ShibbyFileArray fileArray = new ShibbyFileArray(playlistName,
                files.toArray(new ShibbyFile[]{}), null);
        ShibbyFileArrayDialog dialog = new ShibbyFileArrayDialog(
                getContext(), fileArray, (MainActivity)getActivity(), playlistName);
    }

    private void initializeList(View root, final List<String> playlists)
    {
        list = root.findViewById(R.id.listPlaylists);
        list.post(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(null);
                mItemTouchHelper = new ItemTouchHelper(callback);
                
                listLayoutManager = new LinearLayoutManager(getContext());
                list.setLayoutManager(listLayoutManager);
                listAdapter = new ShibbyPlaylistAdapter(getContext(),
                        playlists, ((MainActivity)getActivity()),
                        mItemTouchHelper, true);
                list.setAdapter(listAdapter);
                listAdapter.setClickListener(PlaylistsFragment.this);
                
                callback.setAdapter(listAdapter);
                mItemTouchHelper.attachToRecyclerView(list);
            }
        });
    }

    private void createPlaylist(String playlistName)
    {
        if (PlaylistManager.addPlaylist((MainActivity)getActivity(), playlistName))
        {
            listAdapter.addItem(playlistName);
            listAdapter.notifyDataSetChanged();
            Toast.makeText((MainActivity)getActivity(),
                    "Created playlist", Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText((MainActivity)getActivity(),
                    "Playlist already exists", Toast.LENGTH_LONG).show();
        }
    }

    private void showCreatePlaylistDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Playlist name");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                createPlaylist(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        builder.show();
    }
}