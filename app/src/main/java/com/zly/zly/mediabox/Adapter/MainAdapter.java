package com.zly.zly.mediabox.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhangLuyao on 2017/3/28.
 */

public class MainAdapter extends FragmentPagerAdapter {
    List<Fragment> lists = new ArrayList<>();

    public List<Fragment> getLists() {
        return lists;
    }

    public void setLists(List<Fragment> lists) {
        this.lists = lists;
    }

    public MainAdapter(FragmentManager fm, List<Fragment> lists) {
        super(fm);
        this.lists = lists;
    }

    @Override
    public Fragment getItem(int position) {
        return lists.get(position);
    }

    @Override
    public int getCount() {
        return lists.size();
    }


}
