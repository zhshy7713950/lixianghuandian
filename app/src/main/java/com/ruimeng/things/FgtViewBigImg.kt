package com.ruimeng.things

import android.os.Bundle
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fgt_view_big_img.*
import wongxd.base.BaseBackFragment

/**
 * Created by wongxd on 2019/1/9.
 */
class FgtViewBigImg : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_view_big_img

    companion object {
        fun newInstance(imgPath: String, title: String = "查看大图"): FgtViewBigImg {
            val fgt = FgtViewBigImg()
            val b = Bundle()
            b.putString("imgPath", imgPath)
            b.putString("title", title)
            fgt.arguments = b
            return fgt
        }
    }


    private val imgPath by lazy { arguments?.getString("imgPath") ?: "" }
    private val title by lazy { arguments?.getString("title") }
    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, title)

        Glide.with(this)
            .load(imgPath)
            .into(pinchIv)
    }

}