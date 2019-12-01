package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;

public class ShibbyFileArrayDialog extends Dialog implements ShibbyFileAdapter.ItemClickListener,
        ShibbyPlaylistFileAdapter.ItemClickListener
{
    private String playlistName;

    private RecyclerView list;
    private ShibbyFileAdapter listAdapter;
    private ShibbyPlaylistFileAdapter listAdapterPlaylists;
    private LinearLayoutManager listLayoutManager;
    private ShibbyFileArray fileArray;
    private MainActivity mainActivity;

    public ShibbyFileArrayDialog(Context context, ShibbyFileArray fileArray,
                                 MainActivity mainActivity, String playlistName)
    {
        super(context);
        this.fileArray = fileArray;
        this.mainActivity = mainActivity;
        this.playlistName = playlistName;
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
            listAdapterPlaylists = new ShibbyPlaylistFileAdapter(getContext(),
                    playlistName, fileArray.getFiles(), mainActivity);
            list.setAdapter(listAdapterPlaylists);
            listAdapterPlaylists.setClickListener(ShibbyFileArrayDialog.this);
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
        title.setText(file.getName());
        TextView description = dialog.findViewById(R.id.txtDescription);
        description.setText(file.getDescription());
        dialog.show();
    }
}
