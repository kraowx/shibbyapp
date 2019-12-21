package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;
import io.github.kraowx.shibbyapp.ui.SimpleItemTouchHelperCallback;

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
            createFileInfoDialog(listAdapterPlaylists.getItem(position));
        }
        else
        {
            createFileInfoDialog(listAdapter.getItem(position));
        }
    }

    private void initializeList()
    {
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
                    fileArray.getFiles(), mainActivity);
            list.setAdapter(listAdapter);
            listAdapter.setClickListener(ShibbyFileArrayDialog.this);
        }
        show();
    }

    private void createFileInfoDialog(ShibbyFile file)
    {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.file_info_dialog);
        dialog.setTitle("File Info");
        TextView title = dialog.findViewById(R.id.txtTitle);
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(mainActivity);
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
