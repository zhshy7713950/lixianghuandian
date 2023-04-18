package com.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions


object GlideHelper {

    fun loadImage(context: Context?, imageView: ImageView?, imageUrl: Any?) {
        val options = RequestOptions()
        val requestBuilder = Glide.with(context!!).load(imageUrl).apply(options)
        requestBuilder.into(imageView!!)

    }

    fun loadImageWithNoCache(context: Context?, imageView: ImageView?, imageUrl: Any?) {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)//不做磁盘缓存
            .skipMemoryCache(false)//不做内存缓存
        val requestBuilder = Glide.with(context!!).load(imageUrl).apply(options)
        requestBuilder.into(imageView!!)

    }

}