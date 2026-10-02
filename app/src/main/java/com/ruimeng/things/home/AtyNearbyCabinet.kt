package com.ruimeng.things.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flyco.dialog.listener.OnBtnClickL
import com.flyco.dialog.widget.NormalDialog
import com.ruimeng.things.R
import com.ruimeng.things.net_station.LocationSource
import com.ruimeng.things.net_station.LocationUtil
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.activity_nearby_cabinet.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import wongxd.base.AtyBase
import wongxd.http

class AtyNearbyCabinet : AtyBase() {
    private data class Cabinet(val name: String, val code: String, val address: String)
    private var cabinets = emptyList<Cabinet>()
    private var selectedCode: String? = null
    private var locating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nearby_cabinet)
        initTopbar(topbar, "选择临近站点")
        btn_refresh.setOnClickListener { locateAndLoad() }
        btn_confirm.setOnClickListener { confirmSelection() }
        ensureLocationPermission()
    }

    private fun ensureLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locateAndLoad()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 301)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 301) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) locateAndLoad()
            else finish()
        }
    }

    private fun locateAndLoad() {
        if (locating) return
        locating = true
        lifecycleScope.launch {
            val location = LocationUtil.resolveLocation(this@AtyNearbyCabinet)
            locating = false
            if (isFinishing || isDestroyed) return@launch
            if (location.source != LocationSource.LIVE) {
                ToastHelper.shortToast(this@AtyNearbyCabinet, "定位失败，请点击刷新重试")
                return@launch
            }
            http {
                url = "/apiv6/cgstationnetwork/nearbycabinet"
                params["lat"] = location.coordinates.latitude.toString()
                params["lng"] = location.coordinates.longitude.toString()
                onSuccess { response ->
                    cabinets = parseCabinets(response)
                    selectedCode = null
                    showCabinets()
                    if (cabinets.isEmpty()) ToastHelper.shortToast(this@AtyNearbyCabinet, "附近暂无可用站点")
                }
                onFail { _, message -> ToastHelper.shortToast(this@AtyNearbyCabinet, message) }
            }
        }
    }

    private fun parseCabinets(response: String): List<Cabinet> {
        val data = try { JSONObject(response).opt("data") } catch (_: Exception) { null }
        val result = mutableListOf<Cabinet>()
        fun visit(value: Any?) {
            when (value) {
                is JSONArray -> for (i in 0 until value.length()) visit(value.opt(i))
                is JSONObject -> {
                    val code = listOf("code", "cabinet_code", "cabinetCode", "cabinet_no")
                        .map { value.optString(it) }.firstOrNull { it.isNotBlank() && it != "null" }
                    if (!code.isNullOrBlank()) {
                        result.add(Cabinet(value.optString("site_name", value.optString("siteName", value.optString("name"))),
                            code, value.optString("address", value.optString("site_address"))))
                    } else {
                        value.opt("list")?.let { visit(it) }
                        value.opt("cabinets")?.let { visit(it) }
                        if (!value.has("list") && !value.has("cabinets")) {
                            val keys = value.keys()
                            while (keys.hasNext()) visit(value.opt(keys.next()))
                        }
                    }
                }
            }
        }
        visit(data)
        return result.distinctBy { it.code }
    }

    private fun showCabinets() {
        station_list.removeAllViews()
        cabinets.forEach { cabinet ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_nearby_cabinet, station_list, false)
            card.findViewById<TextView>(R.id.station_name).text = cabinet.name
            val addressView = card.findViewById<TextView>(R.id.station_address)
            addressView.text = cabinet.address
            val codeView = card.findViewById<TextView>(R.id.station_code)
            val highlighted = SpannableString(cabinet.code)
            if (cabinet.code.length >= 4) {
                val from = cabinet.code.length - 4
                highlighted.setSpan(ForegroundColorSpan(0xFFF4DE67.toInt()), from, cabinet.code.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                highlighted.setSpan(RelativeSizeSpan(18f / 14f), from, cabinet.code.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            codeView.text = highlighted
            alignIconWithFirstLine(card.findViewById(R.id.station_code_icon), codeView)
            alignIconWithFirstLine(card.findViewById(R.id.station_address_icon), addressView)
            card.setOnClickListener { selectedCode = cabinet.code; updateSelection() }
            station_list.addView(card)
        }
        updateSelection()
    }

    private fun alignIconWithFirstLine(icon: ImageView, text: TextView) {
        text.post {
            val layout = text.layout ?: return@post
            if (layout.lineCount == 0) return@post
            val firstLineHeight = layout.getLineBottom(0) - layout.getLineTop(0)
            icon.translationY = ((firstLineHeight - icon.height) / 2f).coerceAtLeast(0f)
        }
    }

    private fun updateSelection() {
        for (i in 0 until station_list.childCount) {
            val card = station_list.getChildAt(i)
            val selected = cabinets[i].code == selectedCode
            card.setBackgroundResource(if (selected) R.drawable.bg_nearby_card_selected else R.drawable.bg_nearby_card)
            card.findViewById<ImageView>(R.id.station_check).setImageResource(
                if (selected) R.drawable.ic_scan_selected else R.drawable.ic_scan_unselected)
        }
    }

    private fun confirmSelection() {
        val code = selectedCode
        if (code == null) {
            ToastHelper.shortToast(this, "请您先选择站点")
            return
        }
        NormalDialog(this).apply {
            style(NormalDialog.STYLE_TWO)
            btnNum(2)
            title(code)
            content("请对比以上号码，是否与电柜二维码下方编号一致")
            btnText("确认", "取消")
            setOnBtnClickL(OnBtnClickL {
                dismiss()
                setResult(Activity.RESULT_OK, Intent().putExtra("code", code))
                finish()
            }, OnBtnClickL { dismiss() })
        }.show()
    }
}
