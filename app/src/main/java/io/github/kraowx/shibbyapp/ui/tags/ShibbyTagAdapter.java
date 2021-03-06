package io.github.kraowx.shibbyapp.ui.tags;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;

public class ShibbyTagAdapter extends RecyclerView.Adapter<ShibbyTagAdapter.ViewHolder>
{
    private String searchText;
    private List<ShibbyFileArray> mData, mDataOrig;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    ShibbyTagAdapter(Context context, List<ShibbyFileArray> data)
    {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<ShibbyFileArray>)((ArrayList<ShibbyFileArray>)mData).clone();
        searchText = "";
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.array_list_row, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ShibbyFileArray filearr = mData.get(position);
        String name = filearr.getName();
        if (!searchText.isEmpty() &&
                name.toLowerCase().contains(searchText.toLowerCase()))
        {
            int index = name.toLowerCase().indexOf(searchText.toLowerCase());
            String sub = name.substring(index, index + searchText.length());
            name = name.replace(sub, "<font color=red>" + sub + "</font>");
        }
        holder.txtName.setText(Html.fromHtml(name));
        holder.txtFileCount.setText(filearr.getFileCount() + " files");
    }
    
    @Override
    public int getItemCount()
    {
        return mData.size();
    }
    
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        TextView txtName, txtFileCount;

        ViewHolder(View itemView)
        {
            super(itemView);
            txtName = itemView.findViewById(R.id.arrayName);
            txtFileCount = itemView.findViewById(R.id.txtFileCount);
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
    }

    public void filterDisplayItems(String text)
    {
        searchText = text;
        mData.clear();
        if (text.isEmpty())
        {
            mData.addAll(mDataOrig);
        }
        else
        {
            text = text.toLowerCase();
            for (ShibbyFileArray fileArr : mDataOrig)
            {
                if (fileArr.getName().toLowerCase().contains(text))
                {
                    mData.add(fileArr);
                }
            }
        }
        notifyDataSetChanged();
    }
    
    ShibbyFileArray getItem(int id)
    {
        return mData.get(id);
    }

    void addItem(ShibbyFileArray item)
    {
        mData.add(item);
    }

    void removeItem(ShibbyFileArray item)
    {
        mData.remove(item);
    }

    void setData(List<ShibbyFileArray> files)
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
}
