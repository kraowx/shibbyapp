package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;
import io.github.kraowx.shibbyapp.ui.dialog.FileInfoDialog;
import io.github.kraowx.shibbyapp.ui.playlists.AddFileToPlaylistDialog;
import io.github.kraowx.shibbyapp.ui.playlists.itemtouch.SimpleItemTouchHelperCallback;

public class ShibbyFileArrayDialog extends Dialog implements ShibbyFileAdapter.ItemClickListener,
        ShibbyPlaylistFileAdapter.ItemClickListener
{
    private String playlistName;

    private RecyclerView list;
    private ShibbyFileAdapter listAdapter;
    private ShibbyPlaylistFileAdapter listAdapterPlaylists;
    private LinearLayoutManager listLayoutManager;
    private ItemTouchHelper mItemTouchHelper;
    private ShibbyFileArray fileArray;
    private MainActivity mainActivity;

    public ShibbyFileArrayDialog(Context context, ShibbyFileArray fileArray,
                                 MainActivity mainActivity, String playlistName)
    {
        super(context);
        this.fileArray = fileArray;
        this.mainActivity = mainActivity;
        this.playlistName = playlistName;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.array_info_dialog);
        setTitle(fileArray.getName());
        
        initializeList();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onItemClick(View view, int position)
    {
        if (playlistName != null)
        {
            FileInfoDialog fileInfoDialog = new FileInfoDialog(
                    mainActivity, listAdapterPlaylists.getItem(position),
                    listAdapterPlaylists.getData());
        }
        else
        {
            FileInfoDialog fileInfoDialog = new FileInfoDialog(
                    mainActivity, listAdapter.getItem(position),
                    listAdapter.getData());
        }
    }

    private void initializeList()
    {
        final FloatingActionButton fabAdd = findViewById(R.id.fabAddPlaylist);
        fabAdd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                AddFileToPlaylistDialog dialog = new AddFileToPlaylistDialog(
                        ShibbyFileArrayDialog.this.mainActivity, listAdapter.getCheckedFiles()
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
                            fabAdd.hide();
                        }
                    }
                });
            }
        });
        fabAdd.hide();
        
        list = findViewById(R.id.listArrayDialog);
        list.setHasFixedSize(true);
        TextView title = findViewById(R.id.txtArrInfoDialogTitle);
        title.setText(fileArray.getName());
        listLayoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(listLayoutManager);
        if (playlistName != null)
        {
            SimpleItemTouchHelperCallback callback = new SimpleItemTouchHelperCallback(null);
            mItemTouchHelper = new ItemTouchHelper(callback);
            
            listAdapterPlaylists = new ShibbyPlaylistFileAdapter(getContext(),
                    playlistName, fileArray.getFiles(), mainActivity, mItemTouchHelper);
            list.setAdapter(listAdapterPlaylists);
            listAdapterPlaylists.setClickListener(ShibbyFileArrayDialog.this);
    
            callback.setAdapter(listAdapterPlaylists);
            mItemTouchHelper.attachToRecyclerView(list);
        }
        else
        {
            listAdapter = new ShibbyFileAdapter(getContext(),
                    fileArray.getFiles(), mainActivity, fabAdd);
            list.setAdapter(listAdapter);
            listAdapter.setClickListener(ShibbyFileArrayDialog.this);
        }
        
        show();
    }
}
