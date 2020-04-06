package io.github.kraowx.shibbyapp.ui.playlists;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;

public class AddFileToPlaylistDialog extends Dialog implements ShibbyPlaylistAdapter.ItemClickListener
{
    private boolean showDeleteButton;

    private RecyclerView list;
    private ShibbyPlaylistAdapter listAdapter;
    private LinearLayoutManager listLayoutManager;
    private ShibbyFile[] files;
    private MainActivity mainActivity;
    private FilesAddedListener listener;
    
    public interface FilesAddedListener
    {
        void filesAdded(boolean added);
    }

    public AddFileToPlaylistDialog(MainActivity mainActivity, ShibbyFile[] files,
                                   boolean showDeleteButton)
    {
        super(mainActivity);
        this.files = files;
        this.mainActivity = mainActivity;
        this.showDeleteButton = showDeleteButton;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.playlist_list);
        initializeList(PlaylistManager.getPlaylists(mainActivity));
    }
    
    public void setFilesAddedListener(FilesAddedListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void onItemClick(View view, int position)
    {
        String playlistName = listAdapter.getItem(position);
        if (playlistName.equals("Create New Playlist"))
        {
            showCreatePlaylistDialog();
        }
        else
        {
            addFileToPlaylist(files, playlistName);
        }
    }

    private void initializeList(List<String> playlists)
    {
        list = findViewById(R.id.list);
        list.setHasFixedSize(true);
        listLayoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(listLayoutManager);
        listAdapter = new ShibbyPlaylistAdapter(getContext(),
                playlists, mainActivity, null, showDeleteButton);
        list.setAdapter(listAdapter);
        listAdapter.addItem("Create New Playlist");
        listAdapter.setClickListener(AddFileToPlaylistDialog.this);
        show();
    }

    private void addFileToPlaylist(ShibbyFile[] files, String playlistName)
    {
        if (files.length == 1)
        {
            if (PlaylistManager.addFileToPlaylist(mainActivity, files[0], playlistName))
            {
                Toast.makeText(mainActivity, "File added to playlist",
                        Toast.LENGTH_LONG).show();
                if (listener != null)
                {
                    listener.filesAdded(true);
                }
                this.dismiss();
            }
            else
            {
                Toast.makeText(mainActivity, "File is already in this playlist",
                        Toast.LENGTH_LONG).show();
                if (listener != null)
                {
                    listener.filesAdded(false);
                }
            }
        }
        else if (files.length > 1)
        {
            int added = 0;
            for (ShibbyFile file : files)
            {
                if (PlaylistManager.addFileToPlaylist(mainActivity, file, playlistName))
                {
                    added++;
                }
            }
            if (added > 0)
            {
                Toast.makeText(mainActivity, "Files added to playlist",
                        Toast.LENGTH_LONG).show();
                if (listener != null)
                {
                    listener.filesAdded(true);
                }
                this.dismiss();
            }
            else
            {
                Toast.makeText(mainActivity, "These file are already in this playlist",
                        Toast.LENGTH_LONG).show();
                if (listener != null)
                {
                    listener.filesAdded(false);
                }
            }
        }
    }

    private void createPlaylist(String playlistName)
    {
        if (PlaylistManager.addPlaylist(mainActivity, playlistName))
        {
            listAdapter.removeItem("Create New Playlist");
            listAdapter.addItem(playlistName);
            listAdapter.addItem("Create New Playlist");
            listAdapter.notifyDataSetChanged();
            Toast.makeText(mainActivity, "Created playlist",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(mainActivity, "Playlist already exists",
                    Toast.LENGTH_LONG).show();
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
