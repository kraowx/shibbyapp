package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.ui.playlists.AddFileToPlaylistDialog;

public class ShibbyFileAdapter extends RecyclerView.Adapter<ShibbyFileAdapter.ViewHolder>
{
    private List<ShibbyFile> mData, mDataOrig;
    private MainActivity mainActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private SharedPreferences prefs;

    ShibbyFileAdapter(Context context, List<ShibbyFile> data,
                      MainActivity mainActivity)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<ShibbyFile>)((ArrayList<ShibbyFile>)mData).clone();
        this.mainActivity = mainActivity;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.file_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ShibbyFile file = mData.get(position);
        holder.txtFileName.setText(file.getName());
        if (mainActivity.getDownloadManager().isDownloadingFile(file))
        {
            holder.btnDownload.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.redAccent));
        }
        else if (AudioDownloadManager.fileIsDownloaded(mainActivity, file))
        {
            holder.btnDownload.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.colorAccent));
        }
        else
        {
            boolean darkModeEnabled = prefs
                    .getBoolean("darkMode", false);
            if (darkModeEnabled)
            {
                holder.btnDownload.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
            }
            else
            {
                holder.btnDownload.setColorFilter(null);
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    public void filterDisplayItems(String text)
    {
        mData.clear();
        if(text.isEmpty())
        {
            mData.addAll(mDataOrig);
        }
        else
        {
            text = text.toLowerCase();
            for(ShibbyFile file : mDataOrig)
            {
                if(file.getName().toLowerCase().contains(text))
                {
                    mData.add(file);
                }
            }
        }
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtFileName;
        ImageButton btnPlay, btnDownload, btnAddToPlaylist;

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
                    audioController.setQueue(mData, false);
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
                        mainActivity.getDownloadManager().downloadFile(file, btnDownload);
                        btnDownload.setColorFilter(ContextCompat
                                .getColor(mainActivity, R.color.redAccent));
                    }
                    else if (mainActivity.getDownloadManager().isDownloadingFile(file))
                    {
                        if (mainActivity.getDownloadManager().cancelDownload(file))
                        {
                            boolean darkModeEnabled = prefs
                                    .getBoolean("darkMode", false);
                            if (darkModeEnabled)
                            {
                                btnDownload.setColorFilter(ContextCompat
                                        .getColor(mainActivity, R.color.grayLight));
                            }
                            else
                            {
                                btnDownload.setColorFilter(null);
                            }
                            Toast.makeText(mainActivity, "Download cancelled",
                                    Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(mainActivity, "Failed to cancel download",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        new AlertDialog.Builder(context)
                                .setTitle("Delete download")
                                .setMessage("Are you sure you want to delete this file?")
                                .setPositiveButton(android.R.string.yes,
                                        new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        AudioDownloadManager.deleteFile(mainActivity, file);
                                        boolean darkModeEnabled = prefs
                                                .getBoolean("darkMode", false);
                                        if (darkModeEnabled)
                                        {
                                            btnDownload.setColorFilter(ContextCompat
                                                    .getColor(mainActivity, R.color.grayLight));
                                        }
                                        else
                                        {
                                            btnDownload.setColorFilter(null);
                                        }
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(R.drawable.ic_warning)
                                .show();
                    }
                }
            });
            btnAddToPlaylist = view.findViewById(R.id.btnAddToPlaylist);
            btnAddToPlaylist.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    ShibbyFile file = getItem(getAdapterPosition());
                    showAddFileToPlaylistDialog(file);
                }
            });
            boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
            if (darkModeEnabled)
            {
                btnPlay.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
                btnDownload.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
                btnAddToPlaylist.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
            }
        }
    }

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

    void setData(List<ShibbyFile> files)
    {
        mData = files;
    }

    void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }

    private void showAddFileToPlaylistDialog(ShibbyFile file)
    {
        AddFileToPlaylistDialog dialog = new AddFileToPlaylistDialog(
                mainActivity, file, false);
    }
}
