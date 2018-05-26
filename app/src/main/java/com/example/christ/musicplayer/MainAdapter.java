package com.example.christ.musicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by christ on 2018/5/17.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<MusicInfo> musicInfos;
    private OnItemClickListener onItemClickListener;
    private int layoutId;
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss");

    public MainAdapter(){}
    public MainAdapter(Context context, ArrayList<MusicInfo> musicInfos, int layoutId){
        this.context = context;
        this.musicInfos = musicInfos;
        this.layoutId = layoutId;
    }
    // 赋值新的List
    public void refresh(ArrayList<MusicInfo> newList){
        musicInfos = newList;
        notifyDataSetChanged();
    }

    // 创建ViewHolder，用于回收不用的view来重复利用
    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new MyViewHolder(view);
    }

    // 更改ViewHolder的view的内容
    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, int position){
        // view内容的更改
        viewHolder.title.setText(musicInfos.get(position).getTitle());
        viewHolder.artist.setText(musicInfos.get(position).getArtist());
        viewHolder.duration.setText(format.format(musicInfos.get(position).getDuration()));

        // 点击事件
        if(onItemClickListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(viewHolder.getAdapterPosition());
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemClickListener.onLongClick(viewHolder.getAdapterPosition());
                    return false;
                }
            });
        }
    }

    // 返回人物列表个数
    @Override
    public int getItemCount(){
        if(musicInfos == null)
            return 0;
        return musicInfos.size();
    }

    // 设置点击监听器
    public interface OnItemClickListener {
        void onClick(int position);
        void onLongClick(int position);
    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private TextView artist;
        private TextView duration;
        public MyViewHolder(View view){
            super(view);
            title = (TextView)view.findViewById(R.id.title);
            artist = (TextView)view.findViewById(R.id.artist);
            duration = (TextView)view.findViewById(R.id.duration);
        }
    }
}
