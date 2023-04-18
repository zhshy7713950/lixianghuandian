package com.ruimeng.things

import android.os.Bundle
import kotlinx.android.synthetic.main.aty_splash.*
import wongxd.Config
import wongxd.Http
import wongxd.base.BaseBackActivity
import wongxd.common.loadBigImg
import wongxd.common.startAty
import wongxd.utils.utilcode.util.ScreenUtils

/**
 * Created by wongxd on 2018/11/19.
 */
class AtySplash : BaseBackActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScreenUtils.setFullScreen(this)
        setContentView(R.layout.aty_splash)
        setSwipeBackEnable(false)
        iv_splash.loadBigImg(R.drawable.splash)



        iv_splash.postDelayed({ doJump() }, 1500)
    }


    private fun doJump() {
        if (Config.getDefault().token.isEmpty()) {
            Http.TOKEN_LOST_FUN.invoke("登陆状态丢失,需要重新登陆")
            return
        } else {
            startAty<AtyMain>(intent?.extras)
        }
        finish()
    }
}