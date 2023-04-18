package com.ruimeng.things.me.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.R
import com.ruimeng.things.me.bean.MyTeamBean
import com.utils.GlideHelper


class MyTeamAdapter(data: List<MyTeamBean.Data>?) :
    BaseQuickAdapter<MyTeamBean.Data, BaseViewHolder>(R.layout.item_my_team, data) {

    override fun convert(holper: BaseViewHolder, bean: MyTeamBean.Data) {

        val imageView = holper.getView<ImageView>(R.id.imageView)
        GlideHelper.loadImage(mContext, imageView, bean.avatar)
        holper.setText(R.id.phoneTextView,bean.username)
        holper.setText(R.id.timeTextView,"注册时间：${bean.created}")

    }

}