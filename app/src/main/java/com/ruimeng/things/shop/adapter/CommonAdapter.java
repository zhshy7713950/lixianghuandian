package com.ruimeng.things.shop.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用适配器
 *
 * @param <T>
 * @author Karision.Gou
 * @date 2016-10-17
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
    protected LayoutInflater mInflater;
    protected Context mContext;
    protected List<T> mDatas;
    protected SparseArray<T> sparseArray;
    protected final int mItemLayoutId;
    private int viewHeight;//整个item的高度

    public int getViewHeight() {
        return viewHeight;
    }

    public void setViewHeight(int viewHeight) {
        this.viewHeight = viewHeight;
    }

    public List<T> getmDatas() {
        return mDatas == null ? (List<T>) new ArrayList<>() : mDatas;
    }

    public void setmDatas(List<T> mDatas) {
        this.mDatas = mDatas;
    }

    private boolean isPositon;

    /**
     * 设置list
     *
     * @param list
     */
    public void setList(List<T> list) {
        if (list != null) {
            if (this.mDatas == null) {
                mDatas = list;
                this.notifyDataSetChanged();
                return;
            }
            if (this.mDatas.size() > 0) {
                this.mDatas.clear();
            }
            this.mDatas.addAll(list);
            this.notifyDataSetChanged();
        }
    }

    public void addList(List<T> list) {
        if (list != null && list.size() > 0) {
            this.mDatas.addAll(list);
            this.notifyDataSetChanged();
        }
    }

    /**
     * 添加一个对象
     *
     * @param data
     */
    public void addObject(T data) {
        if (!this.mDatas.contains(data)) {
            this.mDatas.add(data);
        }
        this.notifyDataSetChanged();
    }

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }

    public CommonAdapter(Context context, List<T> mDatas, int itemLayoutId, boolean isPositon) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
        this.isPositon = isPositon;
    }


    public CommonAdapter(Context context, SparseArray<T> sparseArray,
                         int itemLayoutId) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(mContext);
        this.sparseArray = sparseArray;
        this.mItemLayoutId = itemLayoutId;
    }

    @Override
    public int getCount() {
        if (mDatas != null) {

            return mDatas.size();
        } else {
            if (sparseArray != null)
                return sparseArray.size();
            return 0;
        }
    }

    @Override
    public T getItem(int position) {
        if (mDatas != null) {
            return mDatas.get(position);
        }
        return sparseArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);


        if (isPositon) {
            convert(viewHolder, getItem(position), position);
        } else {
            convert(viewHolder, getItem(position));
        }
        if (viewHeight > 0) {
            ViewGroup.LayoutParams layoutParams = viewHolder.getConvertView().getLayoutParams();
            layoutParams.height = viewHeight;
            viewHolder.getConvertView().setLayoutParams(layoutParams);
        }
        return viewHolder.getConvertView();

    }

    public abstract void convert(ViewHolder helper, T item);

    public void convert(ViewHolder helper, T item, int position) {

    }

    private ViewHolder getViewHolder(int position, View convertView,
                                     ViewGroup parent) {
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }

}