package com.ruimeng.things.me

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.ruimeng.things.FgtMain
import com.ruimeng.things.R
import com.ruimeng.things.home.FgtHome
import com.ruimeng.things.home.bean.BannerInfo
import com.ruimeng.things.me.vm.TicketViewModel
import com.utils.ToastHelper
import kotlinx.android.synthetic.main.fgt_ticket.*
import wongxd.base.MainTabFragment
import wongxd.common.toPOJO
import wongxd.http
import me.yokeyword.fragmentation.SupportFragment
import wongxd.utils.utilcode.util.ScreenUtils
import com.bumptech.glide.Glide
import com.ruimeng.things.App
import com.ruimeng.things.common.BannerHelper
import com.ruimeng.things.home.FgtCouponPurchase
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import wongxd.utils.utilcode.util.SizeUtils

class FgtTicket : MainTabFragment() {
    private val vm: TicketViewModel by viewModels()
    private var hasCouponPackages = false

    override fun getLayoutRes(): Int = R.layout.fgt_ticket

    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        // Init Coupon Area background click listener
        iv_my_coupon.setOnClickListener {
            startFgt(FgtMyCoupon.newInstance())
        }

        // Initialize RecyclerViews
        rv_coupon_packages.layoutManager = LinearLayoutManager(activity)
        rv_coupon_packages.isNestedScrollingEnabled = false
        
        rv_activities.layoutManager = LinearLayoutManager(activity)
        rv_activities.isNestedScrollingEnabled = false

        // Initialize observers
        initObservers()
        // Data fetching is now handled in onSupportVisible() to refresh every time the user enters the page
    }

    private fun initObservers() {
        vm.bannerData.observe(this) { bannerList ->
            if (bannerList.isEmpty()) {
                rv_activities.visibility = View.GONE
            } else {
                rv_activities.visibility = View.VISIBLE
                val adapter = ActivityBannerAdapter(bannerList)
                adapter.setOnItemClickListener { _, _, position ->
                    val bannerInfo = bannerList[position]
                    val linkUrl = bannerInfo.linkUrl
                    if (bannerInfo.opType == 1 && linkUrl.startsWith("couponPurchase://")) {
                        if (hasCouponPackages) {
                            // Since we already fetched the coupon packages, we should find it and pass its data.
                            openCouponPurchaseIfAvailable()
                        } else {
                            ToastHelper.shortToast(context, "暂未查询到优惠券包信息")
                        }
                    } else {
                        // Reuse BannerHelper logic for other types
                        BannerHelper.handleBannerClick(bannerInfo, this@FgtTicket)
                    }
                }
                rv_activities.adapter = adapter
            }
        }
    }

    private fun fetchCouponPackages() {
        http {
            url = "/apiv6/advertisementinfo/getadvertisement"
            params["userId"] = FgtHome.userId
            params["position"] = "1"
            params["lat"] = App.lat.toString()
            params["lng"] = App.lng.toString()

            onSuccess { res ->
                val adInfo = res.toPOJO<BannerHelper.AdvertisementData>().data
                val couponPurchaseAd = adInfo.promotions?.find { ad ->
                    ad.operationData?.type == "couponPurchase"
                }

                if (couponPurchaseAd != null) {
                    hasCouponPackages = true
                    cachedCouponData = couponPurchaseAd.operationData?.data
                    // Show list
                    val adapter = CouponPackageAdapter(cachedCouponData ?: emptyList())
                    adapter.setOnItemClickListener { _, _, position ->
                        startFgt(FgtCouponPurchase.newInstance(listOf(adapter.data[position])))
                    }
                    rv_coupon_packages.adapter = adapter
                    rv_coupon_packages.visibility = View.VISIBLE
                    
                    // Show count hint
                    val innerDataList = cachedCouponData
                    val count = innerDataList?.size ?: 0
                    if (count > 0) {
                        tv_coupon_count_hint.visibility = View.VISIBLE
                        val countStr = count.toString()
                        val hintStr = "已为您找到${countStr}份可购超值券包"
                        val ssb = SpannableStringBuilder(hintStr)
                        val start = hintStr.indexOf(countStr)
                        if (start != -1) {
                            ssb.setSpan(
                                AbsoluteSizeSpan(25, true),
                                start,
                                start + countStr.length,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        tv_coupon_count_hint.text = ssb
                    } else {
                        tv_coupon_count_hint.visibility = View.GONE
                    }
                } else {
                    ToastHelper.shortToast(context, "暂未查询到优惠券包信息")
                    hasCouponPackages = false
                    rv_coupon_packages.visibility = View.GONE
                    tv_coupon_count_hint.visibility = View.GONE
                }
            }

            onFail { _, msg ->
                ToastHelper.shortToast(context, "暂未查询到优惠券包信息")
                hasCouponPackages = false
                rv_coupon_packages.visibility = View.GONE
                tv_coupon_count_hint.visibility = View.GONE
            }
        }
    }

    private fun fetchActivities() {
        vm.fetchBannerData(requireContext(), FgtHome.userId)
    }

    private var cachedCouponData: List<com.entity.remote.OperationInnerData>? = null

    private fun openCouponPurchaseIfAvailable() {
        if (cachedCouponData != null) {
            startFgt(FgtCouponPurchase.newInstance(cachedCouponData))
        } else {
            // Fetch if not cached but has packages (fallback)
            fetchCouponPackagesForClick()
        }
    }
    
    private fun fetchCouponPackagesForClick() {
        http {
            url = "/apiv6/advertisementinfo/getadvertisement"
            params["userId"] = FgtHome.userId
            params["position"] = "1"
            params["lat"] = App.lat.toString()
            params["lng"] = App.lng.toString()

            onSuccess { res ->
                val adInfo = res.toPOJO<BannerHelper.AdvertisementData>().data
                val couponPurchaseAd = adInfo.promotions?.find { ad ->
                    ad.operationData?.type == "couponPurchase"
                }

                if (couponPurchaseAd != null) {
                    startFgt(FgtCouponPurchase.newInstance(couponPurchaseAd.operationData?.data))
                } else {
                    ToastHelper.shortToast(context, "暂未查询到优惠券包信息")
                }
            }
        }
    }

    fun startFgt(toFgt: SupportFragment) {
        (parentFragment as FgtMain).start(toFgt)
    }

    inner class CouponPackageAdapter(data: List<com.entity.remote.OperationInnerData>) : 
        BaseQuickAdapter<com.entity.remote.OperationInnerData, BaseViewHolder>(R.layout.item_coupon_package, data) {
        
        override fun convert(helper: BaseViewHolder, item: com.entity.remote.OperationInnerData) {
            val ivBg = helper.getView<ImageView>(R.id.iv_bg)
            val tvPrice = helper.getView<TextView>(R.id.tv_price)
            val tvDesc = helper.getView<TextView>(R.id.tv_desc)
            
            // Dynamic margins based on screen width
            val screenWidth = ScreenUtils.getScreenWidth()
            // Assume the image width is screenWidth - 24dp (12dp margin on each side)
            val imageWidth = screenWidth - SizeUtils.dp2px(24f)
            // The original image aspect ratio might be around 351:106
            val imageHeight = imageWidth * (106f / 351f)
            
            // 价格：上距30（动态），左距30（动态）
            val lp = tvPrice.layoutParams as ConstraintLayout.LayoutParams
            lp.leftMargin = (imageWidth * (30f / 351f)).toInt() // Assuming design width is 351dp
            lp.topMargin = (imageHeight * (20f / 106f)).toInt() // Assuming design height is 106dp
            tvPrice.layoutParams = lp

            val price = item.price ?: ""
            val description = item.description ?: ""
            helper.setText(R.id.tv_price, price)
            helper.setText(R.id.tv_desc, description)
        }
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        activity?.let {
            fetchCouponPackages()
            fetchActivities()
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {

        }
    }

    inner class ActivityBannerAdapter(data: List<BannerInfo>) : 
        BaseQuickAdapter<BannerInfo, BaseViewHolder>(R.layout.item_activity_banner, data) {
        
        override fun convert(helper: BaseViewHolder, item: BannerInfo) {
            val ivBanner = helper.getView<ImageView>(R.id.iv_banner)
            val layoutParams = ivBanner.layoutParams
            layoutParams.height = (ScreenUtils.getScreenWidth() * 0.22f).toInt()
            ivBanner.layoutParams = layoutParams

            Glide.with(mContext)
                .load(item.imgSrc)
                .apply(RequestOptions().transform(
                    CenterCrop(),
                    RoundedCorners(SizeUtils.dp2px(10f))
                ))
                .into(ivBanner)
        }
    }
}