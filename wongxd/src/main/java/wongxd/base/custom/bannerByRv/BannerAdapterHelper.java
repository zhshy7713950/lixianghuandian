package wongxd.base.custom.bannerByRv;

import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;


/**
 * adapter中调用onCreateViewHolder, onBindViewHolder
 * <p>
 * Created by jameson on 9/1/16.
 * <p>
 * changed by 二精-霁雪清虹 on 2017/11/19
 */

public class BannerAdapterHelper {

    public static int sPagePadding = 15;

    public static int sShowLeftCardWidth = 15;


    private float density = Resources.getSystem().getDisplayMetrics().density;


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(float dpValue) {
        return (int) (0.5f + dpValue * Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static float px2dp(float pxValue) {
        return (pxValue / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float px2dip(float pxValue) {
        return (pxValue / density);
    }


    public void onCreateViewHolder(ViewGroup parent, View itemView) {

        RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) itemView.getLayoutParams();

        lp.width = parent.getWidth() - dip2px(2 * (sPagePadding + sShowLeftCardWidth));

        itemView.setLayoutParams(lp);

    }


    public void onBindViewHolder(View itemView, final int position, int itemCount) {

        int padding = dip2px(sPagePadding);

        itemView.setPadding(padding, 0, padding, 0);

        int leftMarin = position == 0 ? padding + dip2px(sShowLeftCardWidth) : 0;

        int rightMarin = position == itemCount - 1 ? padding + dip2px(sShowLeftCardWidth) : 0;

        setViewMargin(itemView, leftMarin, 0, rightMarin, 0);

    }


    private void setViewMargin(View view, int left, int top, int right, int bottom) {

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

        if (lp.leftMargin != left || lp.topMargin != top || lp.rightMargin != right || lp.bottomMargin != bottom) {

            lp.setMargins(left, top, right, bottom);

            view.setLayoutParams(lp);

        }

    }


    public void setPagePadding(int pagePadding) {

        sPagePadding = pagePadding;

    }


    public void setShowLeftCardWidth(int showLeftCardWidth) {

        sShowLeftCardWidth = showLeftCardWidth;

    }




}