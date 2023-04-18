package com.ruimeng.things.shop;


import androidx.annotation.Nullable;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.bean.ItemsBean;

import java.util.List;

public class RecommendAdapter extends BaseQuickAdapter<ItemsBean, BaseViewHolder> {


    public RecommendAdapter(@Nullable List<ItemsBean> data) {
        super(R.layout.item_recommend_goods_new, data);
    }

    @Override
    protected void convert(BaseViewHolder holder, ItemsBean bean) {
//        oldprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰
        Glide.with(mContext)
                .load(bean.getMaster_image())
                .into((ImageView) holder.getView(R.id.goods_list_image));

        holder.setText(R.id.goods_list_goods_name, bean.getTitle());
        holder.setText(R.id.goods_list_old_price, "¥" + bean.getCoupon_price());

    }
}
