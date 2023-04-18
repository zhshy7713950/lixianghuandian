package com.ruimeng.things.me.credit

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import com.ruimeng.things.FgtViewBigImg
import com.ruimeng.things.Path
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.me.FgtUploadAuthInfo
import com.ruimeng.things.me.credit.bean.CreditContractInfoBean
import com.utils.DensityHelper
import kotlinx.android.synthetic.main.fgt_credit_contract.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import wongxd.base.BaseBackFragment
import wongxd.base.custom.anylayer.AnyLayer
import wongxd.common.*
import wongxd.http
import java.lang.ref.WeakReference

/**
 * Created by wongxd on 2019/1/2.
 */
class FgtCreditContract : BaseBackFragment() {

    companion object {
        fun newInstance(contractId: String): FgtCreditContract {
            val fgt = FgtCreditContract()
            val b = Bundle()
            b.putString("contractId", contractId)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_credit_contract

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "合同签约")
        progressDlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "加载中", false)
        progressDlg?.show()
        getContractInfo()
    }


    private fun getContractInfo() {
        http {
            url = Path.CREDITCONTRACT
            params["contract_id"] = contractId

            onFinish {
                progressDlg?.dismissWithAnimation()
            }

            onSuccess {
                val bean = it.toPOJO<CreditContractInfoBean>()
                val data = bean.data
                vp_credit_contract?.let {

                    tv_money_credit_contract.text = "合同金额：¥" + data.loanamount
                    tv_cycle_credit_contract.text = "分期期数：" + data.loanperiod + "期"

                    vp_credit_contract.adapter = Adapter(this@FgtCreditContract, data.imgs.map { it.img })
                    vp_credit_contract.setPageTransformer(false, ScaleTransformer(activity!!))
                    vp_credit_contract.offscreenPageLimit = 2
                    vp_credit_contract.pageMargin = DensityHelper.dp2px(40f)
                    vp_credit_contract.currentItem = 0



                    btn_credit_contract.setOnClickListener {
                        AnyLayer.with(activity!!)
                            .contentView(R.layout.layout_credit_contract_bind_phone)
                            .backgroundBlurScale(8f)
                            .backgroundBlurRadius(8f)
                            .bindData { anyLayer ->
                                val tvPhone = anyLayer.getView<TextView>(R.id.tv_phone)
                                val etCode = anyLayer.getView<EditText>(R.id.et_code_bind_card)
                                val tvGetCode = anyLayer.getView<TextView>(R.id.tv_get_code_bind_card)
                                val btn = anyLayer.getView<QMUIRoundButton>(R.id.btn_credit_contract)

                                tvPhone.text = data.mobile

                                tvGetCode.setOnClickListener {

                                    http {
                                        url = Path.CREDITCODE
                                        params["contract_id"] = contractId
                                        params["mobile"] = tvPhone.text.toString()

                                        onSuccessWithMsg { res, msg ->
                                            EasyToast.DEFAULT.show(msg)
                                            SmsTimeUtils.startCountdown(WeakReference(tvGetCode))
                                        }
                                    }


                                }


                                btn.setOnClickListener {

                                    if (etCode.text.toString().isBlank()) {
                                        EasyToast.DEFAULT.show("请填写验证码")
                                    } else {
                                        tryToSign(etCode.text.toString(), anyLayer)
                                    }

                                }

                            }
                            .show()
                    }
                }

            }

            onFail { code, msg ->
                //                errcode=901
                if (code == 901) {
                    //跳转到上传身份证信息
                    startWithPop(FgtUploadAuthInfo.newInstance(contractId))
                }
            }
        }
    }


    private var progressDlg: SweetAlertDialog? = null

    private fun tryToSign(code: String, anyLayer: AnyLayer) {
        progressDlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "加载中", false)
        progressDlg?.show()
        http {
            url = Path.CREDITSIGN
            params["contract_id"] = contractId
            params["code"] = code

            onFinish {
                progressDlg?.dismissWithAnimation()
            }


            onFail { code, msg ->
                getSweetDialog(SweetAlertDialog.ERROR_TYPE, msg).show()
            }

            onSuccessWithMsg { res, msg ->
                // credit_firstpay int 是否已经完成首付 0否1是   根据credit_firstpay=0则跳转到首付界面，金额提示 首付金额，带上contract_id 即可
                // credit_first_money decimal 首付金额
                val data = JSONObject(res).optJSONObject("data")
                val contract_id = data.optString("contract_id")
                val credit_first_money = data.optString("credit_first_money")

                val dlg = SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
                dlg.titleText = "操作成功"
                dlg.contentText = "即将进入首付支付"
                dlg.confirmText = "确认"
                dlg.setConfirmClickListener {
                    EventBus.getDefault().post(FgtHome.RefreshMyDeviceList())
                    anyLayer.dismiss()
                    dlg.dismissWithAnimation()
                    startWithPop(FgtCreditFirstPay.newInstance(contract_id, credit_first_money))
                }
                dlg.setCanceledOnTouchOutside(false)
                dlg.setCancelable(false)
                dlg.show()
            }
        }
    }


    inner class ScaleTransformer(private val context: Context) : ViewPager.PageTransformer {
        private val elevation: Float

        init {
            elevation = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20f, context.getResources().getDisplayMetrics()
            )
        }

        override fun transformPage(page: View, position: Float) {
            if (position < -1 || position > 1) {

            } else {
                if (position < 0) {
                    (page as CardView).setCardElevation((1 + position) * elevation)
                } else {
                    (page as CardView).setCardElevation((1 - position) * elevation)
                }
            }
        }
    }


    inner class Adapter internal constructor(val fgt: FgtCreditContract, val list: List<String>) :
        PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount(): Int = list.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(activity).inflate(R.layout.item_vp_credit_contract, container, false)
            val imageView = view.findViewById(R.id.iv) as ImageView
            val tv = view.findViewById(R.id.tv) as TextView
            tv.text = "${position + 1}/${list.size}"
            imageView.setOnClickListener {
                start(
                    FgtViewBigImg.newInstance(
                        list[position],
                        "合同(${position + 1}/${list.size})"
                    )
                )
            }
            imageView.loadImg(list[position])
            container.addView(view)
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }


    }
}