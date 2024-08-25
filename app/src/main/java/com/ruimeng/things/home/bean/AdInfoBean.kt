package com.ruimeng.things.home.bean

import android.os.Parcel
import android.os.Parcelable

class AdInfoBean(
    var `data`: Data = Data(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
){
    data class Data(
        var promotions: List<Promotions> = ArrayList()
    )
    data class Promotions(
        val operationTitle: String?,
        val promotionType: String?,//0-全屏广告，1-弹窗广告
        val mediaType: String?,//0-视频，1-图片
        val mediaURL: String?,
        val operationType: String?,//0-关闭广告，1-跳转APP内对应页面，2-打开APP内网页，3-跳出APP打开浏览器网页
        val operationData: OperationData?,//内部操作时所需数据包【operationData- 字典】（operationType = 1的时候用)
        val operationURL: String?//跳转链接【operationURL - 字符串】（operationType = 2 / 3的时候用)
    )

    data class OperationData(
        val type: String?, //couponPurchase：优惠券购买，couponGift：优惠券赠送
        val data: ArrayList<OperationInnerData>?
    )

    data class OperationInnerData(
        val id: String?,//优惠券包ID
        val price: String?,//优惠券包售卖价格
        val discount: String?,//券包已优惠金额（比如：原价300，折后49，优惠251）
        val description: String?//券包描述信息
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )
        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(id)
            parcel.writeString(price)
            parcel.writeString(discount)
            parcel.writeString(description)
        }
        override fun describeContents(): Int {
            return 0
        }
        companion object CREATOR : Parcelable.Creator<OperationInnerData> {
            override fun createFromParcel(parcel: Parcel): OperationInnerData {
                return OperationInnerData(parcel)
            }

            override fun newArray(size: Int): Array<OperationInnerData?> {
                return arrayOfNulls(size)
            }
        }
    }
}
