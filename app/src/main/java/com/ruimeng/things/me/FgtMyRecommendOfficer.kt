package com.ruimeng.things.me

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.ruimeng.things.R
import com.ruimeng.things.ScanQrCodeActivity
import com.uuzuche.lib_zxing.activity.CodeUtils
import kotlinx.android.synthetic.main.fgt_bind_recommend_officer.iv_card_bg
import kotlinx.android.synthetic.main.fgt_bind_recommend_officer.iv_qrcode
import kotlinx.android.synthetic.main.fgt_my_recommend_officer.*
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.loadImg
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import wongxd.common.toPOJO
import wongxd.http
import java.io.Serializable

class FgtMyRecommendOfficer : BaseBackFragment() {

    companion object {
        private const val REQUEST_CODE_SCAN = 1001
    }

    override fun getLayoutRes(): Int = R.layout.fgt_my_recommend_officer

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "我的推荐官")

        fetchRecommendInfo()

        btn_add_officer.setOnClickListener {
            startScan()
        }

        btn_change_officer.setOnClickListener {
            startScan()
        }

        btn_become_officer.setOnClickListener {
            start(FgtRecommendGift())
        }
    }

    private fun startScan() {
        getPermissions(
            activity,
            PermissionType.CAMERA,
            allGranted = {
                val intent = Intent(activity, ScanQrCodeActivity::class.java)
                intent.putExtra("type", "推荐官")
                startActivityForResult(intent, REQUEST_CODE_SCAN)
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCAN && resultCode == android.app.Activity.RESULT_OK) {
            val code = data?.getStringExtra(CodeUtils.RESULT_STRING)
            if (!code.isNullOrBlank()) {
                fetchRecommendInfo(code)
            } else {
                EasyToast.DEFAULT.show("未获取到有效编码")
            }
        }
    }

    private fun fetchRecommendInfo(recmCode: String? = null) {
        http {
            url = "/apiv6/distribute/getrecom"
            if (!recmCode.isNullOrBlank()) {
                params["recmCode"] = recmCode
            }
            onSuccess { res ->
                val response = res.toPOJO<RecommendOfficerBean>()
                val data = response.data

                if (recmCode != null) {
                    // Check if we got valid data for binding
                    if (data != null && !data.recmCode.isNullOrBlank() && data.recmCode != "00000000") {
                        val dialog = DialogBindRecommendOfficer.newInstance(data)
                        dialog.onAction = { action ->
                            if (action == DialogBindRecommendOfficer.RESULT_REFRESH) {
                                fetchRecommendInfo()
                            } else if (action == DialogBindRecommendOfficer.RESULT_GO_GIFT) {
                                fetchRecommendInfo()
                                start(FgtRecommendGift())
                            }
                        }
                        dialog.show(childFragmentManager, "bind_officer")
                    } else {
                        EasyToast.DEFAULT.show("没有找到对应的推荐官信息，请重新操作")
                    }
                } else {
                    updateUI(data)
                }
            }
            onFail { _, msg ->
                EasyToast.DEFAULT.show(msg)
                if (recmCode == null) {
                    updateUI(null)
                }
            }
        }
    }

    private fun updateUI(data: RecommendOfficerBean.Data?) {
        val hasOfficer = data != null &&
                !data.recmCode.isNullOrBlank() &&
                data.recmCode != "00000000"

        if (hasOfficer && data != null) {
            group_no_officer.visibility = View.GONE
            group_has_officer.visibility = View.VISIBLE

            tv_recm_code.text = data.recmCode
            tv_realname.text = if(data.realname.isNullOrEmpty()) "未实名用户" else data.realname
            tv_mobile.text = data.mobile

            var qrUrl = data.qrcodeUrl
            if (!qrUrl.startsWith("http")) {
                qrUrl = "https$qrUrl"
            }
            iv_qrcode.loadImg(qrUrl)

            iv_card_bg.post {
                val bgHeight = iv_card_bg.height
                if (bgHeight > 0) {
                    val params = iv_qrcode.layoutParams
                    params.height = (bgHeight * 0.60).toInt()
                    params.width = params.height
                    iv_qrcode.layoutParams = params
                }
            }

        } else {
            group_no_officer.visibility = View.VISIBLE
            group_has_officer.visibility = View.GONE
        }
    }
}

data class RecommendOfficerBean(
    val code: Int,
    val msg: String,
    val data: Data?
) {
    data class Data(
        val recmCode: String?,
        val qrcodeUrl: String,
        val realname: String,
        val mobile: String
    ): Serializable
}
