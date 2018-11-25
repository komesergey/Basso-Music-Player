package com.basso.basso.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.basso.basso.R;
import com.basso.basso.model.FileMixed;
import com.basso.basso.ui.MusicHolder;
import java.util.ArrayList;
import java.util.List;

public class FileAdepter extends BaseAdapter {

    private final int mLayoutId;

    private final List<FileMixed> data;

    private final Context mContext;
    public FileAdepter(final Context context, final int layoutId, final List<FileMixed> data){
        this.mContext = context;
        this.mLayoutId = layoutId;
        this.data = data;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        MusicHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mLayoutId, parent, false);
            holder = new MusicHolder(convertView);
            holder.mLineTwo.get().setVisibility(View.GONE);
            convertView.setTag(holder);
        } else {
            holder = (MusicHolder)convertView.getTag();
        }
        if(data.get(position).isDirectory()){
            holder.mImage.get().setImageResource(R.drawable.folder_big_icon);
            holder.mLineOne.get().setText(data.get(position).getDirectoryName());
        } else {
            holder.mImage.get().setVisibility(View.GONE);
            holder.mLineOne.get().setText(data.get(position).getTRACK());
        }
        return convertView;

    }

    public void removeItem(int position){
        if(data != null) data.remove(position);
    }
    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }
    @Override
    public FileMixed getItem(int position){
        return data.get(position);
    }

    public List<FileMixed> getData(){
        List<FileMixed> result = new ArrayList<>();
        for(FileMixed in : data){
            if(!in.isDirectory())
                result.add(in);
        }
        return result;
    }
    @Override
    public int getCount(){
        return data.size();
    }
}
