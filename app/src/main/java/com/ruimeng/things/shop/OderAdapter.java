package com.ruimeng.things.shop;

import android.content.Context;
import android.widget.ImageView;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.adapter.CommonAdapter;
import com.ruimeng.things.shop.adapter.ViewHolder;
import com.ruimeng.things.shop.bean.OderListBean;
import wongxd.common.AnyKt;

import java.util.List;


/**
 * Created by dww on 2017-09-24.
 */

public class OderAdapter extends CommonAdapter<OderListBean> {
    public OderAdapter(Context context, List<OderListBean> mDatas, int itemLayoutId) {
        super(context, mDatas, itemLayoutId);
    }

    @Override
    public void convert(ViewHolder helper, OderListBean item) {
        helper.setText(R.id.title_product, item.getTitle());
        helper.setText(R.id.taobao_price, "原价：¥" + item.getOld_price());
        helper.setText(R.id.taobao_xiaoliang, item.getOld_price() + "元");
        helper.setText(R.id.dikou, "已抵扣：" + item.getCoupon_price() + "元");
        helper.setVisible(R.id.phone_money, false);
//        helper.setText (R.id.phone_money,"话费："+item.getNts_balance()+"元");
        ImageView product_img = helper.getView(R.id.product_img);
        AnyKt.loadImg(product_img, item.getPic());
    }
}
