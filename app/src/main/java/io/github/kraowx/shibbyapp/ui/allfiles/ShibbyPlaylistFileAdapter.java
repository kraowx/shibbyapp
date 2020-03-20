package io.github.kraowx.shibbyapp.ui.allfiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.audio.AudioController;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.AudioDownloadManager;
import io.github.kraowx.shibbyapp.tools.DataManager;
import io.github.kraowx.shibbyapp.tools.PlaylistManager;
import io.github.kraowx.shibbyapp.ui.playlists.itemtouch.ItemTouchHelperAdapter;

public class ShibbyPlaylistFileAdapter
        extends RecyclerView.Adapter<ShibbyPlaylistFileAdapter.ViewHolder>
        implements ItemTouchHelperAdapter
{
    private String playlistName;
    private List<ShibbyFile> mData;
    private MainActivity mainActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ItemTouchHelper itemTouchHelper;
    private Context context;
    private SharedPreferences prefs;

    ShibbyPlaylistFileAdapter(Context context, String playlistName, List<ShibbyFile> data,
                              MainActivity mainActivity, ItemTouchHelper itemTouchHelper)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.playlistName = playlistName;
        this.mData = data;
        this.mainActivity = mainActivity;
        this.itemTouchHelper = itemTouchHelper;
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
    }
    
    @Override
    public boolean onItemMove(int fromPosition, int toPosition)
    {
        Collections.swap(mData, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        PlaylistManager.setPlaylistFileData(mainActivity,
                playlistName, mData);
        return true;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(
                R.layout.file_list_row_playlist, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position)
    {
        ShibbyFile file = mData.get(position);
        boolean displayLongNames = prefs.getBoolean(
                "displayLongNames", false);
        boolean showSpecialPrefixTags = prefs.getBoolean(
                "showSpecialPrefixTags", true);
        String name = "";
        if ((file.getType().equals("patreon") ||
                file.isPatreonFile()) && showSpecialPrefixTags)
        {
            int color = mainActivity.getResources().getColor(R.color.redAccent);
            String hex = String.format("#%06X", (0xFFFFFF & color));
            name += " <font color=" + hex + ">[Patreon]</font> ";
        }
        else if (file.getType().equals("user") && showSpecialPrefixTags)
        {
            int color = mainActivity.getResources().getColor(R.color.colorAccent);
            String hex = String.format("#%06X", (0xFFFFFF & color));
            name += " <font color=" + hex + ">[User]</font> ";
        }
        name += displayLongNames ? file.getName() : file.getShortName();
        holder.txtFileName.setText(Html.fromHtml(name));
        if (mainActivity.getDownloadManager().isDownloadingFile(file))
        {
            holder.btnDownload.setColorFilter(ContextCompat
                    .getColor(mainActivity, R.color.redAccent));
        }
        else if (AudioDownloadManager.fileIsDownloaded(mainActivity, file) ||
                file.getType().equals("user"))
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
    
        if (itemTouchHelper != null)
        {
            holder.layout.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    itemTouchHelper.startDrag(holder);
                    return true;
                }
            });
        }
    }
    
    @Override
    public int getItemCount()
    {
        return mData.size();
    }

    
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtFileName;
        ImageButton btnPlay, btnDownload, btnRemoveFromPlaylist;
        LinearLayout layout;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.itemName);
            layout = (LinearLayout)itemView;
            itemView.setOnClickListener(this);
            initializeButtons(itemView);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null)
            {
                mClickListener.onItemClick(view, getAdapterPosition());
            }
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
                    audioController.setQueue(mData, true);
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
                    if (!(AudioDownloadManager.fileIsDownloaded(mainActivity, file) ||
                            file.getType().equals("user")))
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
                        Drawable darkIcon = ContextCompat.getDrawable(mainActivity,
                                R.drawable.ic_warning).mutate();
                        darkIcon.setColorFilter(new ColorMatrixColorFilter(new float[]
                                {
                                        -1, 0, 0, 0, 200,
                                        0, -1, 0, 0, 200,
                                        0, 0, -1, 0, 200,
                                        0, 0, 0, 1, 0
                                }));
                        boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
                        AlertDialog.Builder builder;
                        if (darkModeEnabled)
                        {
                            builder = new AlertDialog.Builder(mainActivity, R.style.DialogThemeDark);
                            builder.setIcon(darkIcon);
                        }
                        else
                        {
                            builder = new AlertDialog.Builder(mainActivity);
                            builder.setIcon(R.drawable.ic_warning);
                        }
                        String title = "Delete ";
                        String message = "Are you sure you want to delete this file?";
                        if (file.getType().equals("user"))
                        {
                            title += "user file";
                            message += " You will have to re-import it if " +
                                    "you want to listen to it again.";
                        }
                        else
                        {
                            title += "download";
                        }
                        builder.setTitle(title)
                                .setMessage(message)
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
                                                if (file.getType().equals("user"))
                                                {
                                                    new DataManager(mainActivity).removeUserFile(file);
                                                    PlaylistManager.removeFileFromPlaylist(
                                                            mainActivity, file, playlistName);
                                                    mData.remove(file);
                                                    notifyDataSetChanged();
                                                }
                                            }
                                        })
                                .setNegativeButton(android.R.string.no, null)
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
            boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
            if (darkModeEnabled)
            {
                btnPlay.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
                btnDownload.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
                btnRemoveFromPlaylist.setColorFilter(ContextCompat
                        .getColor(mainActivity, R.color.grayLight));
            }
        }

        private void showRemoveFileFromPlaylistDialog()
        {
            Drawable darkIcon = ContextCompat.getDrawable(mainActivity,
                    R.drawable.ic_warning).mutate();
            darkIcon.setColorFilter(new ColorMatrixColorFilter(new float[]
                    {
                            -1, 0, 0, 0, 200,
                            0, -1, 0, 0, 200,
                            0, 0, -1, 0, 200,
                            0, 0, 0, 1, 0
                    }));
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(mainActivity);
            boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
            AlertDialog.Builder builder;
            if (darkModeEnabled)
            {
                builder = new AlertDialog.Builder(mainActivity,
                        R.style.DialogThemeDark_Alert);
                builder.setIcon(darkIcon);
            }
            else
            {
                builder = new AlertDialog.Builder(mainActivity);
                builder.setIcon(R.drawable.ic_warning);
            }
            builder.setTitle("Remove file")
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
                    .show();
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
    
    List<ShibbyFile> getData()
    {
        return mData;
    }
    
    void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }
    
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
