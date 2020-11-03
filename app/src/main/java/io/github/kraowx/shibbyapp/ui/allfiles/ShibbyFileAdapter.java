package io.github.kraowx.shibbyapp.ui.allfiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ColorMatrixColorFilter;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.tools.PatreonTier;
import io.github.kraowx.shibbyapp.ui.ListItemBarView;

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
    private FloatingActionButton fabAdd;

    public ShibbyFileAdapter(Context context, List<ShibbyFile> data,
                      MainActivity mainActivity, FloatingActionButton fabAdd)
    {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<ShibbyFile>)((ArrayList<ShibbyFile>)mData).clone();
        this.mainActivity = mainActivity;
        this.fabAdd = fabAdd;
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
        boolean showSpecialPrefixTags = prefs.getBoolean(
                "showSpecialPrefixTags", true);
        String name = "";
        if (file.getTier().getTier() > PatreonTier.FREE && showSpecialPrefixTags)
        {
            int color = mainActivity.getResources().getColor(R.color.redAccent);
            String hex = String.format("#%06X", (0xFFFFFF & color));
            name += " <font color=" + hex + ">[Patreon]</font> ";
        }
        else if (file.getTier().getTier() == PatreonTier.USER && showSpecialPrefixTags)
        {
            int color = mainActivity.getResources().getColor(R.color.colorAccent);
            String hex = String.format("#%06X", (0xFFFFFF & color));
            name += " <font color=" + hex + ">[User]</font> ";
        }
        if (file.getAudienceType() != null)
        {
            name += String.format("[%s] ", file.getAudienceType());
        }
        name += file.getName();
        if (searchText != null && !searchText.isEmpty() &&
                name.toLowerCase().contains(searchText.toLowerCase()))
        {
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            String sub = name.substring(index, index + searchText.length());
            name = name.replace(sub, "<font color=red>" + sub + "</font>");
        }
        String audioType = file.getAudioType();
        if (audioType.contains("Variant ("))
        {
            int start = audioType.indexOf("Variant (")+9;
            int end = audioType.indexOf(")", start);
            String variant = audioType.substring(start, end);
            name += String.format(" <font color=darkgray>(%s)</font>", variant);
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
        
        if (mainActivity.getPatreonSessionManager().getTier().greaterThanEquals(file.getTier()))
        {
            holder.imgViewTypeLock.setVisibility(View.GONE);
        }
        else
        {
            holder.imgViewTypeLock.setVisibility(View.VISIBLE);
            boolean darkModeEnabled = prefs.getBoolean("darkMode", false);
            if (darkModeEnabled)
            {
                holder.imgViewTypeLock.setColorFilter(new ColorMatrixColorFilter(new float[]
                        {
                                -1, 0, 0, 0, 200,
                                0, -1, 0, 0, 200,
                                0, 0, -1, 0, 200,
                                0, 0, 0, 1, 0
                        }));
            }
        }
        
        holder.actionBox.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CheckBox actionBox = (CheckBox)v;
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
    
    public void filterDisplayItems(String text, int[] fileTypes,
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
                for (int type : fileTypes)
                {
                    if (file.getTier().getTier() == type)
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
        ImageView imgViewTypeLock;
        CheckBox actionBox;
        ListItemBarView barView;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtFileName = itemView.findViewById(R.id.itemName);
            itemView.setOnClickListener(this);
            imgViewTypeLock = itemView.findViewById(R.id.imgViewTypeLock);
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
    
    public void clearCheckedFiles()
    {
        checkedFiles.clear();
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
