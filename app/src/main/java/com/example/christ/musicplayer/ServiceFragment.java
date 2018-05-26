package com.example.christ.musicplayer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.MediaStore.Audio.Media;
import java.util.ArrayList;

/**
 * Created by christ on 2018/5/17.
 * 服务器的音乐资源
 */

public class ServiceFragment extends Fragment {
    private ArrayList<String> mTitleList = new ArrayList<>(2);
    private ArrayList<Fragment> mFragments = new ArrayList<>(2);
    private TabLayout tabLayout;
    private ViewPager viewPager;

    public ServiceFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_base, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tab);
        viewPager = (ViewPager) view.findViewById(R.id.vp_list);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initFragmentList();
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getChildFragmentManager(),
                mFragments, mTitleList);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(fragmentAdapter);
    }

    private void initFragmentList() {
        mTitleList.add("歌曲");
        mTitleList.add("录音");
        mFragments.add(new ListFragment());
//        mFragments.add(new ListFragment());
        mFragments.add(ListFragment.newInstance(Media.TITLE+" LIKE '%-record%'",
                new String[]{}));
    }
}
