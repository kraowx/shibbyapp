package io.github.kraowx.shibbyapp.ui.series;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import io.github.kraowx.shibbyapp.R;
import io.github.kraowx.shibbyapp.models.ShibbyFile;
import io.github.kraowx.shibbyapp.models.ShibbyFileArray;

public class ShibbySeriesAdapter extends RecyclerView.Adapter<ShibbySeriesAdapter.ViewHolder>
{
    private List<ShibbyFileArray> mData, mDataOrig;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    ShibbySeriesAdapter(Context context, List<ShibbyFileArray> data)
    {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mDataOrig = (List<ShibbyFileArray>)((ArrayList<ShibbyFileArray>)mData).clone();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = mInflater.inflate(R.layout.array_list_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        ShibbyFileArray filearr = mData.get(position);
        holder.txtName.setText(filearr.getName());
        holder.txtFileCount.setText(filearr.getFileCount() + " files");
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
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
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
            for(ShibbyFileArray fileArr : mDataOrig)
            {
                if(fileArr.getName().toLowerCase().contains(text))
                {
                    mData.add(fileArr);
                }
            }
        }
        notifyDataSetChanged();
    }

    // convenience method for getting data at click position
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
}
