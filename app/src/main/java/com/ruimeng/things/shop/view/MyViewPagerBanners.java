package com.ruimeng.things.shop.view;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ruimeng.things.R;
import wongxd.common.AnyKt;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luomin
 */
public class MyViewPagerBanners extends LinearLayout {

    /**
     * 上下文
     */
    private Context mContext;

    /**
     * 图片轮播视图
     */
    private ViewPager mAdvPager = null;

    /**
     * 滚动图片视图适配器
     */
    private ImageCycleAdapter mAdvAdapter;

    /**
     * 图片轮播指示器控件
     */
    private ViewGroup mGroup;

    /**
     * 图片轮播指示器-个图
     */
    private ImageView mImageView = null;

    /**
     * 滚动图片指示器-视图列表
     */
    private ImageView[] mImageViews = null;
    int imageCount;

    /**
     * 手机密度
     */
    private float mScale;

    /**
     * @param context
     */
    public MyViewPagerBanners(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public MyViewPagerBanners(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mScale = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.ad_cycle_view, this);
        mAdvPager = (ViewPager) findViewById(R.id.adv_pager);
        mAdvPager.addOnPageChangeListener(new GuidePageChangeListener());
        mAdvPager.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        // 开始图片滚动
                        startImageTimerTask();
                        break;
                    default:
                        // 停止图片滚动
                        stopImageTimerTask();
                        break;
                }
                return false;
            }
        });
        // 滚动图片右下指示器视图
        mGroup = (ViewGroup) findViewById(R.id.viewGroup);
    }

    public ViewPager getViewPager() {
        return mAdvPager;
    }

    /**
     * 装填图片数据
     *
     * @param imageUrlList
     * @param PagerBannersListener
     */
    public void setImageResources(List<String> imageUrlList, PagerBannersListener PagerBannersListener) {
        // 清除所有子视图
        mGroup.removeAllViews();
        // 图片广告数量
        imageCount = imageUrlList.size();
        mImageViews = new ImageView[imageCount];
        for (int i = 0; i < imageCount; i++) {
            mImageView = new ImageView(mContext);
            int imageParams = (int) (mScale * 20 + 0.5f);// XP与DP转换，适应不同分辨率
            int imagePadding = (int) (mScale * 5 + 0.5f);
            // 设置原电间距
            LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            // mImageView.setLayoutParams(new LayoutParams(imageParams,
            // imageParams));
            mImageView.setPadding(imagePadding, imagePadding, imagePadding,
                    imagePadding);
            mImageView.setLayoutParams(layoutParams);

            mImageViews[i] = mImageView;
            if (i == 0) {
                mImageViews[i].setBackgroundResource(R.drawable.page_indicator_select);
            } else {
                mImageViews[i].setBackgroundResource(R.drawable.page_indicator_unselect);
            }
            mGroup.addView(mImageViews[i]);
        }
        mAdvAdapter = new ImageCycleAdapter(mContext, imageUrlList, PagerBannersListener);
        mAdvPager.setAdapter(mAdvAdapter);
        // startImageTimerTask();
//        if (imageCount != 1) {
//            mAdvPager.setCurrentItem(100);
//        }
    }

    /**
     * 开始轮播(手动控制自动轮播与否，便于资源控制)
     */
    public void startImageCycle() {
        if (imageCount < 2) {
            return;
        }
        startImageTimerTask();
    }

    /**
     * 暂停轮播——用于节省资源
     */
    public void pushImageCycle() {
        if (imageCount < 2) {
            return;
        }
        stopImageTimerTask();
    }

    /**
     * 开始图片滚动任务
     */
    private void startImageTimerTask() {
        stopImageTimerTask();
        // 图片每3秒滚动一次
        // mHandler.postDelayed(mImageTimerTask, 3000);
        mHandler.sendEmptyMessageDelayed(1, 3000);
    }

    /**
     * 停止图片滚动任务
     */
    private void stopImageTimerTask() {
        // mHandler.removeCallbacks(mImageTimerTask);
        mHandler.removeMessages(1);
    }

    /**
     * 图片自动轮播Task
     */

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    int totalCount = imageCount;
                    int currentItem = mAdvPager.getCurrentItem();
                    int toItem = currentItem + 1 % totalCount;
                    mAdvPager.setCurrentItem(toItem, true);
                    // 每3秒钟发送一个message，用于切换viewPager中的图片
                    this.sendEmptyMessageDelayed(1, 3000);
                    break;
            }
        }
    };

    /**
     * 轮播图片状态监听器
     *
     * @author luomin
     */
    private final class GuidePageChangeListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
            if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
                // startImageTimerTask(); // 开始下次计时
                if (arg0 == ViewPager.SCROLL_STATE_DRAGGING) {
                    if (mAdvPager.getCurrentItem() == imageCount - 1) {
                        mAdvPager.setCurrentItem(0);
                    } else if (mAdvPager.getCurrentItem() == 0) {
                        mAdvPager.setCurrentItem(imageCount);
                    }
                }
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int index) {
            if (imageCount == 0) {
                return;
            }
            index = index % imageCount;
            // 设置图片滚动指示器背景
            mImageViews[index].setBackgroundResource(R.drawable.page_indicator_select);
            for (int i = 0; i < mImageViews.length; i++) {
                if (index != i) {
                    mImageViews[i].setBackgroundResource(R.drawable.page_indicator_unselect);
                }
            }

        }
    }

    private class ImageCycleAdapter extends PagerAdapter {

        /**
         * 图片视图缓存列表
         */
        private ArrayList<ImageView> mImageViewCacheList;

        /**
         * 图片资源列表
         */
        private List<String> mAdList = new ArrayList<>();

        /**
         * 广告图片点击监听器
         */
        private PagerBannersListener mPagerBannersListener;

        private Context mContext;

        public ImageCycleAdapter(Context context, List<String> adList, PagerBannersListener PagerBannersListener) {
            mContext = context;
            mAdList = adList;
            mPagerBannersListener = PagerBannersListener;
            mImageViewCacheList = new ArrayList<>();
        }

        @Override
        public int getCount() {
            if (mAdList.size() == 1) {
                return mAdList.size();
            } else {
                return Integer.MAX_VALUE;
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            ImageView imageView = null;
            int index = 0;
            try {
                index = position % mAdList.size();
//                Log.i("data===","index==="+index);
                String imageUrl = mAdList.get(index);
//                Log.i("data===","imageUrl==="+imageUrl);

                if (mImageViewCacheList.isEmpty()) {
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                } else {
                    imageView = mImageViewCacheList.remove(0);
                }
                // 设置图片点击监听
                final int finalIndex = index;
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPagerBannersListener.onImageClick(finalIndex, v);
                    }
                });
                container.addView(imageView);
                AnyKt.loadImg(imageView, imageUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ImageView view = (ImageView) object;
            container.removeView(view);
            mImageViewCacheList.add(view);
        }

    }

    /**
     * 播放监听事件
     */
    public interface PagerBannersListener {
        /**
         * 单击图片事件
         *
         * @param position
         * @param imageView
         */
        void onImageClick(int position, View imageView);
    }

}
