package com.ruimeng.things.adapter

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

class BannerImageCommonAdapter(dataList: List<String>) : BannerImageAdapter<String>(dataList) {
    override fun onBindView(
        holder: BannerImageHolder,
        data: String,
        position: Int,
        size: Int
    ) {
        Glide.with(holder.itemView)
            .load(data)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(10)))
            .into(holder.imageView)
    }
}