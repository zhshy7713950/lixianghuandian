package com.ruimeng.things.shop;

import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.bean.NewResultBean;

import java.util.List;


public class GoodsListAdapter extends BaseQuickAdapter<NewResultBean.ItemBean, BaseViewHolder> {


    public GoodsListAdapter(@Nullable List<NewResultBean.ItemBean> data) {
        super(R.layout.item_goods_list, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, NewResultBean.ItemBean bean) {
        Glide.with(mContext)
                .load(bean.getMaster_image())
                .into((ImageView) holder.getView(R.id.goods_list_image));
        //0 淘宝 1天猫 2京东
        if ("0".equals(bean.getItem_type())) {
            holder.setBackgroundRes(R.id.goods_list_goods_type, R.drawable.goods_taobao);
        } else if ("1".equals(bean.getItem_type())) {
            holder.setBackgroundRes(R.id.goods_list_goods_type, R.drawable.goods_tianmao);
        } else if ("2".equals(bean.getItem_type())) {
            holder.setBackgroundRes(R.id.goods_list_goods_type, R.drawable.goods_jingdong);
        } else if ("3".equals(bean.getItem_type())) {
            holder.setBackgroundRes(R.id.goods_list_goods_type, R.drawable.goods_pinduoduo);
        }
        holder.setText(R.id.goods_list_goods_name, bean.getTitle());
        TextView oldprice = holder.getView(R.id.goods_list_old_price);
        oldprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰
        oldprice.setText("¥" + bean.getOld_price());
        holder.setText(R.id.goods_list_saled_number, bean.getSales() + "人付款");
        holder.setText(R.id.goods_list_new_price, "券后¥" + bean.getEnd_price());
        holder.setText(R.id.goods_list_coupons_price, "可抵扣" + bean.getCoupon_price() + "元");

    }
}
