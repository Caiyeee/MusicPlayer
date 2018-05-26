package com.example.christ.musicplayer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

/**
 * Created by christ on 2018/5/17.
 */

public class ListFragment extends Fragment {

    private Context mContext;
    private RecyclerView mRecyclerView;
    private MainAdapter mMainAdapter;
    private ArrayList<MusicInfo> musicInfos;
    private ScaleInAnimationAdapter animationAdapter;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event){
        // 刷新
        if(event.equals("refresh")){
            musicInfos.clear();
            loadData();
            mMainAdapter.refresh(musicInfos);
            animationAdapter.notify();
        }
    }

    public ListFragment(){
    }

    private static final String ARG_PARAM1 = "selection";
    private static final String ARG_PARAM2 = "selectionArgs";
    private String mParam1;
    private String[] mParam2;
    private boolean getArgs;
    public static ListFragment newInstance(String selection, String[] selectionArgs) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, selection);
        args.putStringArray(ARG_PARAM2, selectionArgs);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getStringArray(ARG_PARAM2);
            getArgs = true;
        } else {
            getArgs = false;
        }
        Log.e("create", getArgs ? mParam1 : "none");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);

        // 注册订阅者
        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadData();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext)); // 显示
        mMainAdapter = new MainAdapter(mContext, musicInfos, R.layout.item);
        //增加动画效果
        animationAdapter = new ScaleInAnimationAdapter(mMainAdapter);
        animationAdapter.setDuration(700);
        mRecyclerView.setAdapter(animationAdapter);

        mMainAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                ((MainActivity)getActivity()).getMessage(position,musicInfos);
                Log.e("record url", musicInfos.get(position).getUrl());

//                Intent intent = new Intent(mContext, TestActivity.class);
//                intent.putExtra("musicInfo", musicInfos.get(position));
//                startActivity(intent);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
    }

    private void loadData(){
        if(!getArgs){
            musicInfos = MediaUtil.query(mContext.getContentResolver(), null, null);
        } else {
            musicInfos = MediaUtil.query(mContext.getContentResolver(),
                    mParam1, mParam2);
        }

    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        Log.e("destroy", getArgs ? mParam1 : "none");
    }
}
