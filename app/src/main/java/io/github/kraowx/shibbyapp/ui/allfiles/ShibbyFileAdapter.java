package io.github.kraowx.shibbyapp.ui.allfiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;

public class ShibbyFileAdapter extends RecyclerView.Adapter<ShibbyFileAdapter.ViewHolder>
{
    private String searchText;
    private List<ShibbyFile> mData, mDataOrig;
    private List<ShibbyFile> checkedFiles;
    private MainActivity mainActivity;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;
    private SharedPreferences prefs;

    public ShibbyFileAdapter(Context context, List<ShibbyFile> data,
                      MainActivity mainActivity)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<ShibbyFile>)((ArrayList<ShibbyFile>)mData).clone();
        this.mainActivity = mainActivity;
        checkedFiles = new ArrayList<ShibbyFile>();
        prefs = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        searchText = "";
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
        final ShibbyFile file = mData.get(position);
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
        if (searchText != null && !searchText.isEmpty() &&
                name.toLowerCase().contains(searchText.toLowerCase()))
        {
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            String sub = name.substring(index, index + searchText.length());
            name = name.replace(sub, "<font color=red>" + sub + "</font>");
        }
        holder.txtFileName.setText(Html.fromHtml(name));
        
        if (checkedFiles.contains(file))
        {
            holder.actionBox.setChecked(true);
        }
        else
        {
            holder.actionBox.setChecked(false);
        }
        
        holder.actionBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CheckBox actionBox = (CheckBox)v;
                FloatingActionButton fabAdd =
                        mainActivity.findViewById(R.id.fabAddPlaylist);
                
                if (actionBox.isChecked() && !checkedFiles.contains(file))
                {
                    checkedFiles.add(file);
                }
                else if (checkedFiles.contains(file))
                {
                    checkedFiles.remove(file);
                }
                
                if (checkedFiles.size() == 1)
                {
                    fabAdd.show();
                }
                else if (checkedFiles.size() == 0)
                {
                    fabAdd.hide();
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mData.size();
    }
    
    public void filterDisplayItems(String text, String[] fileTypes,
                                   int[] durations, String[] tags)
    {
        searchText = text;
        int k;
        int total = (fileTypes != null ? 1 : 0) + (durations != null ? 1 : 0) +
                ((tags != null && tags.length > 0) ? 1 : 0) +
                ((text != null && !text.isEmpty()) ? 1 : 0);
        mData.clear();
        for (int i = 0; i < mDataOrig.size(); i++)
        {
            ShibbyFile file = mDataOrig.get(i);
            k = 0;
            if (fileTypes != null)
            {
                for (String type : fileTypes)
                {
                    if (file.getType().equals(type))
                    {
                        k++;
                        break;
                    }
                }
            }
            if (durations != null)
            {
                for (int dur : durations)
                {
                    float duration = file.getDuration() / (float) (60 * 1000);
                    if ((dur == 60 && duration > 60))
                    {
                        k++;
                        break;
                    }
                    else if (dur < 60 && duration > dur && duration <= dur + 20)
                    {
                        k++;
                        break;
                    }
                }
            }
            if (text != null && !text.isEmpty())
            {
                text = text.toLowerCase();
                if (file.getName().toLowerCase().contains(text) ||
                        file.matchesTag(text))
                {
                    k++;
                }
            }
            if (tags != null && tags.length > 0)
            {
                for (String tag : tags)
                {
                    if (file.hasTag(tag))
                    {
                        k++;
                        break;
                    }
                }
            }
            if (k == total && !mData.contains(file))
            {
                mData.add(file);
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtFileName;
        CheckBox actionBox;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.itemName);
            itemView.setOnClickListener(this);
            actionBox = itemView.findViewById(R.id.actionBox);
        }

        @Override
        public void onClick(View view)
        {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public ShibbyFile getItem(int id)
    {
        return mData.get(id);
    }

    public void addItem(ShibbyFile item)
    {
        mData.add(item);
    }

    public void removeItem(ShibbyFile item)
    {
        mData.remove(item);
    }

    public void setData(List<ShibbyFile> files)
    {
        mData = files;
    }
    
    public List<ShibbyFile> getData()
    {
        return mData;
    }
    
    public List<ShibbyFile> getCheckedFiles()
    {
        return checkedFiles;
    }

    public void setClickListener(ItemClickListener itemClickListener)
    {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener
    {
        void onItemClick(View view, int position);
    }
}
