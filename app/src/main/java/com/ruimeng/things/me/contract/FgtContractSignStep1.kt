package com.ruimeng.things.me.contract

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.FgtViewBigImg
import com.ruimeng.things.Path
import com.ruimeng.things.PathV3
import com.ruimeng.things.R
import com.ruimeng.things.home.ContractCheckEvent
import com.ruimeng.things.home.FgtDeposit
import com.ruimeng.things.me.FgtUploadAuthInfo
import com.ruimeng.things.me.contract.bean.ContractSignStepOneBean
import com.utils.DensityHelper
import com.utils.TextUtil
import kotlinx.android.synthetic.main.fgt_contract_sign_step_1.*
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_deposit_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_device_model_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_device_num_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_rent_long_my_contract_detail
import kotlinx.android.synthetic.main.fgt_my_contract_detail.tv_rent_money_my_contract_detail
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import wongxd.base.BaseBackFragment
import wongxd.base.custom.bannerByRv.BannerRecyclerView.OnPageChangeListener
import wongxd.common.*
import wongxd.http

/**
 * Created by wongxd on 2019/12/30.
 * https://github.com/wongxd
 * wxd1@live.com
 */
class FgtContractSignStep1 : BaseBackFragment() {

    companion object {

        val RESULT_CODE_SHOULD_POP = 1002

        fun newInstance(contractId: String, qStr: String, contractType: Int,pageType:Int = 0,deviceId:String ="",deviceModel:String=""): FgtContractSignStep1 {
            val fgt = FgtContractSignStep1()
            val b = Bundle()
            b.putString("contractId", contractId)
            b.putString("qStr", qStr)
            b.putString("deviceId", deviceId)
            b.putString("deviceModel", deviceModel)
            b.putInt("contractType", contractType)
            b.putInt("pageType", pageType)
            fgt.arguments = b
            return fgt
        }
    }

    override fun getLayoutRes(): Int = R.layout.fgt_contract_sign_step_1

    private val contractId: String by lazy { arguments?.getString("contractId") ?: "" }

    private val qStr by lazy { arguments?.getString("qStr") ?: "" }
    private val deviceModel by lazy { arguments?.getString("deviceModel") ?: "" }
    private val deviceId by lazy { arguments?.getString("deviceId") ?: "" }
    private val contractType by lazy { arguments?.getInt("contractType") ?: 1 }
    // 0 从合约列表进入， 1 查看合约（支付押金、单次购买、续期升级）  2 查看合约需要签名（支付租金）
    private val getPageType by lazy { arguments?.getInt("pageType",0) }
    private val colors = arrayOf("#FFFFFF","#B2C1CE")

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, if (getPageType == 1) "租赁协议" else "合同签约")
        progressDlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "加载中", false)
        progressDlg?.show()
        getContractInfo()
    }


    override fun onFragmentResult(requestCode: Int, resultCode: Int, data: Bundle?) {
        super.onFragmentResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_SHOULD_POP && resultCode == RESULT_CODE_SHOULD_POP) {
            EventBus.getDefault().post(FgtMyContract.EventDoContractSearch(qStr, contractType))
            pop()
        }
    }
//    @Subscribe
//    fun checkContract(event: ContractCheckEvent) {
//        pop()
//    }
    private fun getContractInfo() {

        http {
            url = if (getPageType == 1) PathV3.PAYMENT_SIGN_CONTRACT else PathV3.SIGN_CONTRACT
            if (getPageType != 1){
                params["contract_id"] = contractId
            }
            params["appType"] = "lxhd"
            onFinish {
                progressDlg?.dismissWithAnimation()
            }

            onSuccess { res ->


                vp_sign_contract?.let {


                    val bean = res.toPOJO<ContractSignStepOneBean>()
                    val data = bean.data


                    if (getPageType != 0){
                        layout_battery1.visibility = View.VISIBLE
                        layout_battery2.visibility = View.GONE
                        if (getPageType == 1){
                            tv_battery_num_pay_rent_money.text = deviceId
                            tv_battery_model_pay_rent_money.text = deviceModel
                        }else{
                            tv_battery_num_pay_rent_money.text = "${data.device_id}"
                            tv_battery_model_pay_rent_money.text = "${data.model_str}"
                        }
                    }else{
                        layout_battery1.visibility = View.GONE
                        layout_battery2.visibility = View.VISIBLE
                        tv_device_num_my_contract_detail.text = "电池编号：${data.device_id}"
                        tv_device_model_my_contract_detail.text = "${data.model_str}"
                        tv_rent_long_my_contract_detail.text = "${data.renttime_str}"
                        tv_deposit_my_contract_detail.text = "${data.deposit}元"
                        tv_rent_money_my_contract_detail.text = "${data.rent}元"
                    }



                    vp_sign_contract.apply {
                        adapter =
                            Adapter(this@FgtContractSignStep1, data.sign_pngs.map { it.png })
                        setPageTransformer(false, ScaleTransformer(activity))
                        offscreenPageLimit = 2
                        pageMargin = DensityHelper.dp2px(40f)
                        currentItem = 0


                    }
                    vp_sign_contract.addOnPageChangeListener( object :ViewPager.OnPageChangeListener{
                        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        }
                        override fun onPageSelected(position: Int) {
                            tv_contract_page.text = TextUtil.getSpannableString(arrayOf("合同预览","(${position + 1}/${data.sign_pngs.size})"),colors)
                        }
                        override fun onPageScrollStateChanged(state: Int) {
                        }
                    })
                    indicator.attachToViewPager(vp_sign_contract)
                    tv_contract_page.text = TextUtil.getSpannableString(arrayOf("合同预览","(${1}/${data.sign_pngs.size})"),colors)

                    if (getPageType != 1){
                        object : CountDownTimer((data.wait_sec * 1000).toLong(), 1000.toLong()) {
                            override fun onTick(millisUntilFinished: Long) {
                                MainLooper.runOnUiThread {
                                    btn_sign_contract?.text =
                                        "我已阅读并同意合同内容(${millisUntilFinished / 1000}s)"
                                    btn_sign_contract?.setOnClickListener {}
                                }
                            }
                            override fun onFinish() {
                                MainLooper.runOnUiThread {
                                    btn_sign_contract?.text = "我已阅读并同意合同内容"
                                    btn_sign_contract?.setOnClickListener {
                                        startForResult(
                                            FgtContractSignStep2.newInstance(data.contract_id),
                                            RESULT_CODE_SHOULD_POP
                                        )
                                    }
                                }
                            }
                        }.start()
                    }else{
                        btn_sign_contract?.text = "我已确认"
                        btn_sign_contract.setOnClickListener {
                            EventBus.getDefault().post(ContractCheckEvent(true))
                            pop()
                        }
                    }
                    sv_contract.postDelayed({
                        if (sv_contract != null) {
                            sv_contract.fullScroll(View.FOCUS_DOWN)
                        }
                    },500)
                    sv_contract.postDelayed({
                        if (sv_contract != null){
                            sv_contract.fullScroll(View.FOCUS_UP)
                        }},
                    1500)

                }

            }

            onFail { code, msg ->
                vp_sign_contract?.let {
                    if (code == 901) {
                        //跳转到上传身份证信息
                        startWithPop(FgtUploadAuthInfo.newInstance(contractId))
                    } else
                        EasyToast.DEFAULT.show(msg)
                }
            }
        }
    }


    private var progressDlg: SweetAlertDialog? = null


    inner class ScaleTransformer(context: Context?) : ViewPager.PageTransformer {

        private val elevation: Float = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f, context?.resources?.displayMetrics
        )

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


    inner class Adapter internal constructor(
        val fgt: FgtContractSignStep1,
        val list: List<String>
    ) :
        PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

        override fun getCount(): Int = list.size

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val view = LayoutInflater.from(activity)
                .inflate(R.layout.item_vp_credit_contract, container, false)
            val imageView = view.findViewById(R.id.iv) as ImageView

//            val tv = view.findViewById(R.id.tv) as TextView
//            tv.text = "${position + 1}/${list.size}"`
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