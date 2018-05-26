package com.example.christ.musicplayer;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by christ on 2018/5/15.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragment;
    private List<String> mTitleList;

    /**
     * 普通，主页使用
     */
    public FragmentAdapter(FragmentManager fm, List<Fragment> mFragment) {
        super(fm);
        this.mFragment = mFragment;
    }

    /**
     * 接收首页传递的标题
     */
    public FragmentAdapter(FragmentManager fm, List<Fragment> mFragment, List<String> mTitleList) {
        super(fm);
        this.mFragment = mFragment;
        this.mTitleList = mTitleList;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragment.get(position);
    }

    @Override
    public int getCount() {
        return mFragment.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }

    // 返回tablayout的标题文字;
    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitleList != null) {
            return mTitleList.get(position);
        } else {
            return "";
        }
    }

    public void UpdateFragmentList(List<Fragment> fragment) {
        this.mFragment = fragment;
        notifyDataSetChanged();
    }
    @Override
    public int getItemPosition(Object object) {
        // TODO Auto-generated method stub
        return FragmentStatePagerAdapter.POSITION_NONE;
    }
}