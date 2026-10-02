package com.ruimeng.things.home

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.ruimeng.things.R
import com.ruimeng.things.home.view.ScanMaskView
import com.ruimeng.things.utils.CustomerServiceManager
import com.ruimeng.things.voice.VoicePlayerManager
import com.utils.ToastHelper
import com.utils.WeChatHelper
import com.uuzuche.lib_zxing.activity.CodeUtils
import java.io.IOException

object ScanPageUi {
    const val REQUEST_INPUT = 1
    const val REQUEST_ALBUM = 2
    const val REQUEST_STATION = 3

    fun bind(activity: FragmentActivity, mode: ScanPageMode) {
        val mask = activity.findViewById<ScanMaskView>(R.id.scan_mask)
        mask.follow(activity.findViewById(R.id.scan_window_placeholder))
        activity.findViewById<ScrollView>(R.id.scan_scroll).setOnScrollChangeListener { _, _, _, _, _ ->
            mask.invalidate()
        }
        activity.findViewById<TextView>(R.id.tv_scan_hint).text = "请扫描二维码"
        activity.findViewById<View>(R.id.tv_shortcut_heading).visibility =
            if (mode == ScanPageMode.PICKUP) View.GONE else View.VISIBLE
        activity.findViewById<View>(R.id.shortcut_row).visibility =
            if (mode == ScanPageMode.PICKUP) View.GONE else View.VISIBLE
        activity.findViewById<View>(R.id.btn_station).visibility = if (mode.showStation) View.VISIBLE else View.GONE
        activity.findViewById<View>(R.id.btn_input).visibility = if (mode.showInput) View.VISIBLE else View.GONE
        activity.findViewById<View>(R.id.btn_album).visibility = if (mode.showAlbum) View.VISIBLE else View.GONE

        activity.findViewById<View>(R.id.btn_light).setOnClickListener {
            val label = activity.findViewById<TextView>(R.id.tv_light)
            val turningOn = label.text.toString() == "打开手电筒"
            CodeUtils.isLightEnable(turningOn)
            label.text = if (turningOn) "关闭手电筒" else "打开手电筒"
        }
        activity.findViewById<View>(R.id.btn_input).setOnClickListener {
            activity.startActivityForResult(Intent(activity, AtyInputCode::class.java).apply {
                putExtra("type", when (mode) {
                    ScanPageMode.CABINET -> 3
                    ScanPageMode.COUPON -> 9
                    ScanPageMode.OFFICER -> 8
                    ScanPageMode.PICKUP -> 4
                })
            }, REQUEST_INPUT)
        }
        activity.findViewById<View>(R.id.btn_station).setOnClickListener {
            activity.startActivityForResult(Intent(activity, AtyNearbyCabinet::class.java), REQUEST_STATION)
        }
        activity.findViewById<View>(R.id.btn_album).setOnClickListener {
            activity.startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }, REQUEST_ALBUM)
        }
        activity.findViewById<View>(R.id.btn_service).setOnClickListener {
            CustomerServiceManager.showDialSheet(activity)
        }
        activity.findViewById<View>(R.id.btn_mini_program).setOnClickListener {
            WeChatHelper.launchWXMiniProgram(activity, activity.getString(R.string.wx_appid), "")
        }
        ToastHelper.shortToast(activity, mode.prompt)
        mode.voice?.let { VoicePlayerManager.getInstance().playVoice(activity, it) }
    }

    fun readAlbumCode(activity: Activity, data: Intent?): String? {
        val uri = data?.data ?: return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val sample = generateSequence(1) { it * 2 }.first { bounds.outWidth / it <= 1600 && bounds.outHeight / it <= 1600 }
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val bitmap = activity.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                ?: return null
            try {
                val pixels = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source = RGBLuminanceSource(bitmap.width, bitmap.height, pixels)
                MultiFormatReader().decode(BinaryBitmap(HybridBinarizer(source))).text
            } finally {
                bitmap.recycle()
            }
        } catch (_: Exception) {
            null
        }
    }
}
