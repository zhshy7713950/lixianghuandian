package com.ruimeng.things.shop.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.ruimeng.things.R;


/**
 * Created by zxy on 2016/4/8.
 */
public class RefreshListView extends ListView implements AbsListView.OnScrollListener {

    private OnRefreshListener mListener;
    private View mFootView;
    private LinearLayout foot;
    private int footHeight;
    private ProgressBar mFootImage;
    private TextView textMore;

    public RefreshListView(Context context) {
        super(context);
        initView(context,null,0);
    }

    public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context,attrs,0);
    }

    public void setOnRefreshListener( OnRefreshListener mListener){
        this.mListener = mListener;

    }


    private void initView(Context context, AttributeSet attrs, int defStyleAttr) {
        mFootView = LayoutInflater.from(context).inflate(R.layout.foot_refresh,null);
        addFooterView(mFootView,null,false);
        init();
        mFootView.measure(0, 0);
        footHeight = mFootView.getMeasuredHeight();
        mFootView.setPadding(0,-footHeight,0,0);
        this.setOnScrollListener(this);
    }
    private void init() {

        foot = (LinearLayout) findViewById(R.id.foot);
        mFootImage= (ProgressBar) mFootView.findViewById(R.id.home_foot_refresh);
        textMore = (TextView) mFootView.findViewById(R.id.tv_jiazai_more);
    }

    private boolean isbottom;
    ///////////////////////ListView 的滑动监听///////////////////////
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_FLING){
            if (getLastVisiblePosition() == getCount()-1 && !isbottom){
                mFootView.setPadding(0,0,0,0);
                setSelection(getCount()-1);
                isbottom = true;
                if (mListener != null){
                    mListener.moreLoadingListener();
                }
            }
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public void onRefreshComplete(boolean success){
        mFootView.setPadding(0, -footHeight, 0, 0);
        if(!success){
            isbottom = true;
        }else{
            isbottom=false;
        }
       /* if (isbottom){
            mFootView.setPadding(0,-footHeight,0,0);
            isbottom = false;
        }
        else {
            mFootView.setPadding(0,-footHeight,0,0);
        }*/
    }

    public interface OnRefreshListener{
        void moreLoadingListener();

    }

    public void setTextView (String text) {
        textMore.setText(text);
        mFootImage.setVisibility(View.GONE);
    }
}
