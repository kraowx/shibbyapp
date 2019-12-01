package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;

public class ShibbyPlaylistFileAdapter extends RecyclerView.Adapter<ShibbyPlaylistFileAdapter.ViewHolder>
{
    private String playlistName;

    private List<ShibbyFile> mData;
    private MainActivity mainActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    ShibbyPlaylistFileAdapter(Context context, String playlistName, List<ShibbyFile> data,
                              MainActivity mainActivity)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlistName = playlistName;
        this.mData = data;
        this.mainActivity = mainActivity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.file_list_row_playlist, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ShibbyFile file = mData.get(position);
        holder.txtFileName.setText(file.getName());
        if (AudioDownloadManager.fileIsDownloaded(mainActivity, file))
        {
            holder.btnDownload.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.colorAccent));
        }
        else
        {
            holder.btnDownload.setColorFilter(null);
        }
    }

    // total number of rows
    @Override
    public int getItemCount()
    {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtFileName;
        ImageButton btnPlay, btnDownload, btnRemoveFromPlaylist;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.itemName);
            itemView.setOnClickListener(this);
            initializeButtons(itemView);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        private void initializeButtons(View view)
        {
            btnPlay = view.findViewById(R.id.btnPlay);
            btnPlay.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    AudioController audioController = mainActivity.getAudioController();
                    ShibbyFile file = getItem(getAdapterPosition());
                    audioController.loadFile(file);
                    audioController.setQueue(mData);
                    audioController.setVisible(true);
                }
            });
            btnDownload = view.findViewById(R.id.btnDownload);
            btnDownload.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final ShibbyFile file = getItem(getAdapterPosition());
                    if (!AudioDownloadManager.fileIsDownloaded(mainActivity, file))
                    {
                        AudioDownloadManager.downloadFile(mainActivity, file);
                        btnDownload.setColorFilter(ContextCompat
                                .getColor(mainActivity, R.color.colorAccent));
                    }
                    else
                    {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete download")
                                .setMessage("Are you sure you want to delete this file?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        AudioDownloadManager.deleteFile(mainActivity, file);
                                        btnDownload.setColorFilter(null);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(R.drawable.ic_warning)
                                .show();
                    }
                }
            });
            btnRemoveFromPlaylist = view.findViewById(R.id.btnRemoveFromPlaylist);
            btnRemoveFromPlaylist.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    showRemoveFileFromPlaylistDialog();
                }
            });
        }

        private void showRemoveFileFromPlaylistDialog()
        {
            new AlertDialog.Builder(context)
                    .setTitle("Delete download")
                    .setMessage("Are you sure you want to remove " +
                            "this file from the playlist?")
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    ShibbyFile file = getItem(getAdapterPosition());
                                    removeFileFromPlaylist(file, playlistName);
                                }
                            })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_warning)
                    .show();
        }
    }

    // convenience method for getting data at click position
    ShibbyFile getItem(int id)
    {
        return mData.get(id);
    }

    void addItem(ShibbyFile item)
    {
        mData.add(item);
    }

    void removeItem(ShibbyFile item)
    {
        mData.remove(item);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

    private void removeFileFromPlaylist(ShibbyFile file, String playlistName)
    {
        if (PlaylistManager.removeFileFromPlaylist(mainActivity, file, playlistName))
        {
            mData.remove(file);
            notifyDataSetChanged();
            Toast.makeText(mainActivity, "File removed from playlist",
                    Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(mainActivity, "Failed to remove file from playlist",
                    Toast.LENGTH_LONG).show();
        }
    }
}
