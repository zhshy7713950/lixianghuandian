package com.ruimeng.things.me

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_bind_recommend_officer.*
import wongxd.common.EasyToast
import wongxd.common.loadImg
import wongxd.http
import wongxd.utils.utilcode.util.ScreenUtils

class DialogBindRecommendOfficer : DialogFragment() {

    companion object {
        private const val ARG_DATA = "arg_data"
        const val RESULT_REFRESH = 1
        const val RESULT_GO_GIFT = 2

        fun newInstance(data: RecommendOfficerBean.Data): DialogBindRecommendOfficer {
            val fragment = DialogBindRecommendOfficer()
            val bundle = Bundle()
            bundle.putSerializable(ARG_DATA, data)
            fragment.arguments = bundle
            return fragment
        }
    }

    var onAction: ((Int) -> Unit)? = null
    private var mData: RecommendOfficerBean.Data? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fgt_bind_recommend_officer, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0)
    }

    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val width = (ScreenUtils.getScreenWidth() * 0.85).toInt()
        window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        window?.setGravity(Gravity.CENTER)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mData = arguments?.getSerializable(ARG_DATA) as? RecommendOfficerBean.Data
        if (mData == null) {
            dismiss()
            return
        }

        initUI(mData!!)

        btn_cancel.setOnClickListener {
            onAction?.invoke(RESULT_REFRESH)
            dismiss()
        }

        btn_confirm.setOnClickListener {
            bindOfficer(mData?.recmCode)
        }

        btn_ok.setOnClickListener {
            onAction?.invoke(RESULT_REFRESH)
            dismiss()
        }

        btn_become.setOnClickListener {
            onAction?.invoke(RESULT_GO_GIFT)
            dismiss()
        }

        view?.setOnClickListener {
            dismiss()
        }

        layout_card.setOnClickListener {
            // consume click
        }
    }

    private fun initUI(data: RecommendOfficerBean.Data) {
        tv_recm_code.text = data.recmCode
        tv_realname.text = if(data.realname.isNullOrEmpty()) "未实名用户" else data.realname
        tv_mobile.text = data.mobile

        val qrUrl = normalizeQrUrl(data.qrcodeUrl)
        iv_qrcode.loadImg(qrUrl)

        iv_card_bg.post {
            val bgHeight = iv_card_bg.height
            if (bgHeight > 0) {
                val params = iv_qrcode.layoutParams
                params.height = (bgHeight * 0.6).toInt()
                params.width = params.height
                iv_qrcode.layoutParams = params
            }
        }
    }

    private fun normalizeQrUrl(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) return ""
        if (value.startsWith("http://") || value.startsWith("https://")) return value
        if (value.startsWith("://")) return "https$value"
        return "https://$value"
    }

    private fun bindOfficer(recmCode: String?) {
        if (recmCode.isNullOrBlank()) return

        http {
            url = "/apiv6/distribute/bindrecom"
            params["recmCode"] = recmCode
            onSuccess {
                EasyToast.DEFAULT.show("提交成功，请稍后查看提现结果")
                showSuccessState()
            }
            onFail { _, msg ->
                EasyToast.DEFAULT.show(msg)
            }
        }
    }

    private fun showSuccessState() {
        group_confirm.visibility = View.GONE
        group_success.visibility = View.VISIBLE

        tv_title.text = "恭喜！您已成功添加推荐官"
        tv_title.setTextColor(Color.parseColor("#FECE31"))

        tv_bottom_hint.text = "您也可以成为推荐官，享更多福利"
        tv_bottom_hint.gravity = Gravity.CENTER
    }
}
