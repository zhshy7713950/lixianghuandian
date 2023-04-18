package com.ruimeng.things.shop;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.view.ViewGroup;
import com.ruimeng.things.shop.bean.TabLayoutBean;

import java.util.List;

public class TabLayoutAdapter extends FragmentStatePagerAdapter {

    private List<TabLayoutBean> dataList;
    private List<Fragment> mFragments;

    public TabLayoutAdapter(FragmentManager fm, List<TabLayoutBean> dataList, List<Fragment> mFragments) {
        super(fm);
        if (this.dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
        } else {
            this.dataList = dataList;
        }
        this.mFragments = mFragments;
    }

    @Override
    public Fragment getItem(int postion) {
        return mFragments.get(postion);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return dataList.get(position).getName();
    }

    @Override
    public int getCount() {

//        return dataList.size();
        return mFragments == null ? 0 : mFragments.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
