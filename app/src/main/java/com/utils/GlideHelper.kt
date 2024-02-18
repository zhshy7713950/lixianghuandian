package com.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget


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
    fun loadImageAsBitmap(context: Context?,url: String, callback: (Bitmap?) -> Unit) {
        Glide.with(context!!)
            .asBitmap()
            .load(url)
            .into(object :CustomTarget<Bitmap>(){
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                   callback(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }
}