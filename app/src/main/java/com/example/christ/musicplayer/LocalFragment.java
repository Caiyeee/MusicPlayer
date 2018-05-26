package com.example.christ.musicplayer;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Audio.Media;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

/**
 * Created by christ on 2018/5/17.
 * 本地的音乐资源
 */
public class LocalFragment extends Fragment {
    private ArrayList<String> mTitleList = new ArrayList<>(2);
    private ArrayList<Fragment> mFragments = new ArrayList<>(2);
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FragmentAdapter fragmentAdapter;

    private String artist = null;
    private int artist_id;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(User user){
        // 获取用户
        artist = user.getUsername();
        artist_id = user.getId();
        mFragments.remove(1);
//        mFragments.add(ListFragment.newInstance(Media.TITLE+" LIKE '%-record' AND "
//                +Media.ARTIST + " = " + "'" + artist + "'", new String[]{}));
        mFragments.add(ListFragment.newInstance(Media.TITLE+" LIKE '%-record" + artist_id + "'",
                new String[]{}));
        fragmentAdapter.UpdateFragmentList(mFragments);
    }

    public LocalFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_base, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tab);
        viewPager = (ViewPager) view.findViewById(R.id.vp_list);

        // 注册订阅者
        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initFragmentList();
        fragmentAdapter = new FragmentAdapter(getChildFragmentManager(),
                mFragments, mTitleList);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(fragmentAdapter);
    }

    private void initFragmentList() {
        mTitleList.add("伴奏");
        mTitleList.add("本地录音");
       // mFragments.add(new ListFragment());
       mFragments.add(ListFragment.newInstance(Media.TITLE+" LIKE '%-instru'", new String[]{}));
       mFragments.add(ListFragment.newInstance(Media.TITLE+" LIKE '%-record' AND "
                        +Media.ARTIST + " = " + artist, new String[]{}));
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
