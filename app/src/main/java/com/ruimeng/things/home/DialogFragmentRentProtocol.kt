package com.ruimeng.things.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.ruimeng.things.Path
import com.ruimeng.things.R
import wongxd.base.BaseDialogFragment
import wongxd.http

/**
 * Created by wongxd on 2018/11/13.
 */
class DialogFragmentRentProtocol : BaseDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val v = initView()

        return v
    }

    private fun initView(): View {
        val v = View.inflate(activity, R.layout.dialog_rent_protocol, null)

        val web = v.findViewById<WebView>(R.id.web_rent_protocol)


        http {
            url = Path.RENT_AGREEMENT
            method = "get"
            onResponse {
                web.loadData(it,"text/html","utf-8")
            }
        }

        return v
    }


}