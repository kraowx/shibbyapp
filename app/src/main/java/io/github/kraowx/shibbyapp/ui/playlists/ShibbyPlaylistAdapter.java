package io.github.kraowx.shibbyapp.ui.playlists;

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
import io.github.kraowx.shibbyapp.tools.PlaylistManager;

public class ShibbyPlaylistAdapter extends RecyclerView.Adapter<ShibbyPlaylistAdapter.ViewHolder>
{
    private boolean showDeleteButton;

    private List<String> mData, mDataOrig;
    private MainActivity mainActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private SharedPreferences prefs;

    ShibbyPlaylistAdapter(Context context, List<String> data,
                          MainActivity mainActivity, boolean showDeleteButton)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<String>)((ArrayList<String>)mData).clone();
        this.mainActivity = mainActivity;
        this.showDeleteButton = showDeleteButton;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = null;
        if (showDeleteButton)
        {
            view = mInflater.inflate(R.layout.playlist_list_row,
                    parent, false);
        }
        else
        {
            view = mInflater.inflate(R.layout.playlist_list_row_no_delete,
                    parent, false);
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        String playlist = mData.get(position);
        holder.txtPlaylistName.setText(playlist);
        int fileCount = PlaylistManager.getPlaylistFileCount(mainActivity, playlist);
        if (playlist.equals("Create New Playlist"))
        {
            holder.txtFileCount.setText("");
        }
        else
        {
            holder.txtFileCount.setText(fileCount + " files");
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
            for(String playlist : mDataOrig)
            {
                if(playlist.toLowerCase().contains(text))
                {
                    mData.add(playlist);
                }
            }
        }
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtPlaylistName, txtFileCount;
        ImageButton btnDeletePlaylist;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtPlaylistName = itemView.findViewById(R.id.arrayName);
            txtFileCount = itemView.findViewById(R.id.txtFileCount);
            if (showDeleteButton)
            {
                btnDeletePlaylist = itemView.findViewById(R.id.btnDeletePlaylist);
                btnDeletePlaylist.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showDeleteDialog();
                    }
                });
                boolean darkModeEnabled = prefs
                        .getBoolean("darkMode", false);
                if (darkModeEnabled)
                {
                    btnDeletePlaylist.setColorFilter(ContextCompat
                            .getColor(mainActivity, R.color.grayLight));
                }
            }
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null)
            {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }

        private void deletePlaylist(String playlistName)
        {
            if (PlaylistManager.removePlaylist(mainActivity, playlistName))
            {
                mData.remove(playlistName);
                notifyDataSetChanged();
                Toast.makeText(mainActivity, "Deleted playlist",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(mainActivity, "Failed to delete playlist",
                        Toast.LENGTH_LONG).show();
            }
        }

        private void showDeleteDialog()
        {
            final String playlistName = getItem(getAdapterPosition());
            new AlertDialog.Builder(context)
                    .setTitle("Delete playlist")
                    .setMessage("Are you sure you want to delete " +
                            "the playlist \"" + playlistName + "\"?")
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    deletePlaylist(playlistName);
                                }
                            })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(R.drawable.ic_warning)
                    .show();
        }
    }

    String getItem(int id)
    {
        return mData.get(id);
    }

    void addItem(String item)
    {
        mData.add(item);
    }

    void removeItem(String item)
    {
        mData.remove(item);
    }

    void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }
    
    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }
}
