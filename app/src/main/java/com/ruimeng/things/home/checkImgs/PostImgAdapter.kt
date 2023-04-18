package com.ruimeng.things.home.checkImgs

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruimeng.things.R

/**
 * 图片显示Adapter
 * Created by kuyue on 2017/6/19 下午3:59.
 * 邮箱:595327086@qq.com
 */

class PostImgAdapter(private val mContext: Context, private val mDatas: List<String>?) :
    RecyclerView.Adapter<PostImgAdapter.MyViewHolder>() {
    companion object {
        var IMAGE_SIZE = 9
    }

    private val mLayoutInflater: LayoutInflater

    init {
        this.mLayoutInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(mLayoutInflater.inflate(R.layout.item_rv_post_new, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (position >= IMAGE_SIZE) {//图片已选完时，隐藏添加按钮
            holder.imageView.visibility = View.GONE
        } else {
            holder.imageView.visibility = View.VISIBLE
        }
        Glide.with(mContext).load(mDatas!![position]).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return mDatas?.size ?: 0
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal var imageView: ImageView

        init {
            imageView = itemView.findViewById(R.id.sdv)
        }
    }


}
