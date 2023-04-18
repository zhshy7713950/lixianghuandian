package com.ruimeng.things.me

import android.os.Bundle
import android.view.View
import com.ruimeng.things.Path
import com.ruimeng.things.R
import kotlinx.android.synthetic.main.fgt_bind_card.*
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.common.EasyToast
import wongxd.common.SmsTimeUtils
import wongxd.http
import wongxd.utils.SystemUtils
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2018/11/28.
 */
class FgtBindCard : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_bind_card


    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "绑定银行卡")

        getStatus()

        tv_get_code_bind_card.setOnClickListener {
            val reserve_mobile = et_phone_bind_card.text.toString()

            http {

                params["mobile"] = reserve_mobile
                params["tag"] = "bindcard"


                onSuccessWithMsg { res, msg ->
                    SmsTimeUtils.startCountdown(WeakReference(tv_get_code_bind_card))
                }
            }
        }

        btn_submit_bind_card.setOnClickListener {
            val cardno = et_card_bind_card.text.toString()
            val name = et_name_bind_card.text.toString()
            val reserve_mobile = et_phone_bind_card.text.toString()
            val code = et_code_bind_card.text.toString()

            if (SystemUtils.isHadEmptyText(cardno, name, reserve_mobile, code)) {
                EasyToast.DEFAULT.show("请完善本页数据")
                return@setOnClickListener
            }


            http {
                url = Path.BIND_BANK

                params["cardno"] = cardno
                params["name"] = name
                params["reserve_mobile"] = reserve_mobile
                params["code"] = code

                onSuccessWithMsg { res, msg ->
                    EasyToast.DEFAULT.show(msg)
                    hideAll()
                    ll_status_yet_bind_card.visibility = View.VISIBLE
                    tv_card_bind_card.text = "卡号：$cardno"
                    tv_name_bind_card.text = "姓名：$name"
                }

            }
        }


    }

    private var bindBankId = -1

    private fun getStatus() {

        http {
            this.IS_SHOW_MSG = false
            url = Path.BANK_QUERY

            onSuccess {

                //{"errcode":200,"errmsg":"操作成功","data":{"id":0,"status":0,"bankno":"","name":""}}
                //status 0未绑定1已绑定
                val json = JSONObject(it)
                val data = json.optJSONObject("data")
                val id = data.optInt("id")
                val status = data.optInt("status")
                val bankno = data.optString("bankno")
                val name = data.optString("name")

                if (status == 1) {
                    ll_status_yet_bind_card.visibility = View.VISIBLE
                    tv_card_bind_card.text = "卡号：$bankno"
                    tv_name_bind_card.text = "姓名：$name"


                    btn_unbindbank_bind_card.text = "解除绑定"
                    btn_unbindbank_bind_card.setOnClickListener {

                        if (bindBankId == -1) {
                            EasyToast.DEFAULT.show("状态错误，请返回后重新进入此页面")
                            return@setOnClickListener
                        }

                        http {
                            url = Path.UNBIND_BANK
                            params["id"] = bindBankId.toString()

                            onSuccessWithMsg { res, msg ->
                                statusNoCard()
                            }
                        }
                    }

                } else if (bankno.isNotBlank() && name.isNotBlank()) {
                    ll_status_yet_bind_card.visibility = View.VISIBLE
                    tv_card_bind_card.text = "卡号：$bankno"
                    tv_name_bind_card.text = "姓名：$name"

                    btn_unbindbank_bind_card.text = "审核中，请等待"
                    btn_unbindbank_bind_card.setOnClickListener {}

                } else
                    statusNoCard()

            }

            onFail { code, msg ->
                statusNoCard()
            }
        }
    }


    private fun hideAll() {
        ll_status_no_card_bind_card.visibility = View.GONE
        ll_status_binding_bind_card.visibility = View.GONE
        ll_status_yet_bind_card.visibility = View.GONE
    }

    private fun statusNoCard() {
        hideAll()
        ll_status_no_card_bind_card.visibility = View.VISIBLE

        btn_add_card_bind_card.setOnClickListener {
            hideAll()
            ll_status_binding_bind_card.visibility = View.VISIBLE
        }
    }


}