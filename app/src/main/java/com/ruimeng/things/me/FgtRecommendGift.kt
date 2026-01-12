package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import com.ruimeng.things.InfoViewModel
import com.ruimeng.things.R
import com.ruimeng.things.me.activity.AtyWeb2
import com.ruimeng.things.me.bean.ShareQrCodeBean
import kotlinx.android.synthetic.main.fgt_recommend_gift.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.loadImg
import wongxd.common.toPOJO
import wongxd.http

class FgtRecommendGift : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_recommend_gift
    private var hasRequestedShareQrCode = false

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "推荐有礼")
        topbar.setBackgroundColor(Color.parseColor("#D83D3E"))

        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.right).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }

        btn_share_invite_code.setOnClickListener {
            EasyToast.DEFAULT.show("TODO：生成分享图片")
        }

        btn_my_reward.setOnClickListener {
            EasyToast.DEFAULT.show("TODO：我的奖励页面")
        }

        InfoViewModel.getDefault().userInfo.simpleObserver(this) { userInfo ->
            if (hasRequestedShareQrCode) return@simpleObserver
            hasRequestedShareQrCode = true
            fetchShareQrCode(userInfo.id)
        }
    }

    private fun fetchShareQrCode(userId: String?) {
        http {
            url = "/apiv6/distribute/shareqrcode"
            if (!userId.isNullOrBlank()) {
                params["user_id"] = userId
            }
            onSuccess { res ->
                val data = res.toPOJO<ShareQrCodeBean>().data
                tv_invite_code_value.text = data.recmCode.ifBlank { "--" }
                val qrUrl = normalizeQrUrl(data.qrcodeUrl)
                if (qrUrl.isNotBlank()) {
                    iv_invite_qrcode.loadImg(qrUrl)
                }
            }
            onFail { _, _ ->
                tv_invite_code_value.text = "--"
            }
        }
    }

    private fun normalizeQrUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return ""
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (value.startsWith("//")) return "https:$value"
        return "https://$value"
    }
}
