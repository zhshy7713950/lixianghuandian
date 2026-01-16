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
import com.utils.StatusBarUtil
import com.utils.WeChatHelper
import wongxd.utils.utilcode.util.SizeUtils
import wongxd.utils.utilcode.util.ImageUtils
import java.io.File
import java.io.FileOutputStream
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import com.ruimeng.things.bean.UserInfoBean

class FgtRecommendGift : BaseBackFragment() {

    override fun getLayoutRes(): Int = R.layout.fgt_recommend_gift
    private var hasRequestedShareQrCode = false

    override fun onResume() {
        super.onResume()
        activity?.let { StatusBarUtil.setColor(it, Color.parseColor("#D83D3E")) }
    }

    override fun onDestroy() {
        super.onDestroy()
        activity?.let { StatusBarUtil.setColor(it, resources.getColor(R.color.app_color)) }
    }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)

        initTopbar(topbar, "推荐有礼")
        topbar.setBackgroundColor(Color.parseColor("#D83D3E"))

        topbar.addRightImageButton(R.drawable.ic_question_white, R.id.right).setOnClickListener {
            AtyWeb2.start("规则说明", "http://xianglilai.scxll.cn/appH5/newClientRewardRule.html")
        }

        btn_share_invite_code.setOnClickListener {
            InfoViewModel.getDefault().userInfo.value?.let { userInfo ->
                val inviteCode = tv_invite_code_value.text.toString()
                if (inviteCode == "--" || inviteCode.isBlank()) {
                    EasyToast.DEFAULT.show("暂无邀请码")
                    return@setOnClickListener
                }
                val qrDrawable = iv_invite_qrcode.drawable
                if (qrDrawable == null) {
                    EasyToast.DEFAULT.show("二维码未加载")
                    return@setOnClickListener
                }

                view?.postDelayed({
                    try {
                        val bitmap = generateShareBitmap(userInfo, inviteCode, qrDrawable)
                        showShareUI(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        EasyToast.DEFAULT.show("生成失败: ${e.message}")
                    }
                }, 100)
            }
        }

        btn_my_reward.setOnClickListener {
            start(FgtMyReward())
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
        if (value.startsWith("://")) return "https$value"
        return "https://$value"
    }

    private fun generateShareBitmap(userInfo: UserInfoBean.Data.UserInfo, inviteCode: String, qrDrawable: android.graphics.drawable.Drawable): android.graphics.Bitmap {
        val context = requireContext()
        val layout = android.view.LayoutInflater.from(context).inflate(R.layout.layout_share_poster_gen, null)

        val ivBg = layout.findViewById<ImageView>(R.id.iv_bg)
        val tvInviteCode = layout.findViewById<TextView>(R.id.tv_invite_code)
        val ivQrcode = layout.findViewById<ImageView>(R.id.iv_qrcode)
        val tvName = layout.findViewById<TextView>(R.id.tv_name)

        tvInviteCode.text = inviteCode
        ivQrcode.setImageDrawable(qrDrawable)

        // Name logic: Use nickname, remove content after '-' (including '-')
        var name = userInfo.nickname
        if (name.isBlank()) {
            name = userInfo.username
        }
        // Remove content starting from '-'
        if (name.contains("-")) {
            name = name.substringBefore("-")
        }

        if (name.isBlank()) {
            name = "锂享用户"
        }
        tvName.text = name

        // Measure and Layout
        // Width = Screen Width - 80dp (40dp margin on each side)
        val screenWidth = resources.displayMetrics.widthPixels
        val width = screenWidth

        // Height is determined by the aspect ratio of the background image, or just wrap_content
        // Since it's wrap_content in XML, we measure with MeasureSpec
        layout.measure(
            android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY),
            android.view.View.MeasureSpec.makeMeasureSpec(0, android.view.View.MeasureSpec.UNSPECIFIED)
        )
        layout.layout(0, 0, layout.measuredWidth, layout.measuredHeight)

        val bitmap = android.graphics.Bitmap.createBitmap(layout.measuredWidth, layout.measuredHeight, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        layout.draw(canvas)
        return bitmap
    }

    private fun showShareUI(bitmap: android.graphics.Bitmap) {
        val act = activity ?: return

        // 1. Show Image Dialog
        val dialog = android.app.Dialog(requireContext(), R.style.TransparentDialog)
        // 去除 Dialog 自带的半透明背景，防止颜色叠加过深
        dialog.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val dialogView = android.widget.FrameLayout(requireContext())
        dialogView.setBackgroundColor(Color.parseColor("#CC000000")) // 80% Black

        val imageView = ImageView(requireContext())
        imageView.setImageBitmap(bitmap)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER

        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.gravity = android.view.Gravity.TOP or android.view.Gravity.CENTER_HORIZONTAL
        val margin40 = SizeUtils.dp2px(40f)
        params.leftMargin = margin40
        params.rightMargin = margin40
        params.topMargin = SizeUtils.dp2px(80f)

        dialogView.addView(imageView, params)
        dialog.setContentView(dialogView, android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        ))

        dialogView.setOnClickListener {
            dialog.dismiss()
        }
        imageView.setOnClickListener {
            // Consume click
        }

        dialog.show()
        dialog.window?.let { window ->
            window.decorView.setPadding(0, 0, 0, 0)
            window.setLayout(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 2. Show Bottom Sheet
        val builder = com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet.BottomListSheetBuilder(act)

        val blueSpan = android.text.style.ForegroundColorSpan(Color.parseColor("#007AFF"))
        val redSpan = android.text.style.ForegroundColorSpan(Color.parseColor("#FF3B30"))

        val wechatText = android.text.SpannableString("分享到微信")
        wechatText.setSpan(blueSpan, 0, wechatText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val albumText = android.text.SpannableString("保存到相册")
        albumText.setSpan(blueSpan, 0, albumText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        val cancelText = android.text.SpannableString("取消")
        cancelText.setSpan(redSpan, 0, cancelText.length, android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.addItem(wechatText.toString(), "wechat")
        builder.addItem(albumText.toString(), "album")
        builder.addItem(cancelText.toString(), "cancel")

        builder.setOnSheetItemClickListener { sheet, _, _, tag ->
            sheet.dismiss()
            when (tag) {
                "wechat" -> {
                    WeChatHelper.weChatShareImage(act, getString(R.string.wx_appid), bitmap)
                }
                "album" -> {
                    saveToAlbum(bitmap)
                }
                "cancel" -> {
                    // Do nothing, just dismiss
                }
            }
        }

        val sheet = builder.build()
        // 去除 BottomSheet 自带的半透明背景
        sheet.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        sheet.setOnDismissListener {
            dialog.dismiss()
        }
        sheet.show()
    }

    private fun saveToAlbum(bitmap: android.graphics.Bitmap) {
        val context = context ?: return
        val fileName = "lixianghuandian_${System.currentTimeMillis()}.png"
        val path = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM).absolutePath + File.separator + fileName

        try {
            val file = File(path)
            val fos = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()

            // Notify MediaScanner
            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(file)
                )
            )
            EasyToast.DEFAULT.show("已保存到相册")
        } catch (e: Exception) {
            e.printStackTrace()
            EasyToast.DEFAULT.show("保存失败: ${e.message}")
        }
    }
}
