package com.ruimeng.things

import android.os.Bundle
import android.os.Parcel
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.entity.remote.OperationInnerData
import com.ruimeng.things.net_station.bean.NetStationBean
import me.yokeyword.fragmentation.anim.FragmentAnimator
import me.yokeyword.fragmentation.helper.internal.ResultRecord
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParcelableCompatibilityTest {

    companion object {
        private const val SENTINEL = 0x12345678
    }

    @Test
    fun operationData_roundTripConsumesExactlyItsPayload() {
        val source = OperationInnerData(
            id = "coupon-1",
            price = "49.00",
            discount = "251.00",
            description = "测试券包",
            coupon_type = "rent",
            app_type = "lxhd",
            limit_city = "510100",
            limit_voltage = "72",
            limit_day_desc = "30天",
            act_duration = "2026-12-31"
        )

        assertRoundTrip(source, OperationInnerData.CREATOR) { restored ->
            assertEquals(source, restored)
        }
    }

    @Test
    fun stationData_roundTripPreservesNestedCollections() {
        val source = NetStationBean.Data.X(
            address = "天府五街6号",
            lat = 30.542695,
            lng = 104.059652,
            id = "station-1",
            site_name = "服务中心",
            available_arr = NetStationBean.Data.Model(model_72 = 3, model_60 = 2, model_48 = 1),
            telData = listOf(NetStationBean.Data.TelData("09:00-18:00", "028-12345678")),
            siteImages = listOf("https://example.com/a.jpg"),
            batTypeCount = hashMapOf("72" to hashMapOf("50" to 3)),
            cityId = "510100"
        )

        @Suppress("UNCHECKED_CAST")
        val creator = source.javaClass.getField("CREATOR").get(null)
                as android.os.Parcelable.Creator<NetStationBean.Data.X>
        assertRoundTrip(source, creator) { restored ->
            assertEquals(source, restored)
        }
    }

    @Test
    fun fragmentationState_roundTripConsumesExactlyItsPayload() {
        val animator = FragmentAnimator(1, 2, 3, 4)
        assertRoundTrip(animator, FragmentAnimator.CREATOR) { restored ->
            assertEquals(1, restored.enter)
            assertEquals(2, restored.exit)
            assertEquals(3, restored.popEnter)
            assertEquals(4, restored.popExit)
        }

        val result = ResultRecord().apply {
            requestCode = 7
            resultCode = 200
            resultBundle = Bundle().apply { putString("result", "ok") }
        }
        assertRoundTrip(result, ResultRecord.CREATOR) { restored ->
            assertEquals(7, restored.requestCode)
            assertEquals(200, restored.resultCode)
            assertEquals("ok", restored.resultBundle.getString("result"))
        }
    }

    private fun <T : android.os.Parcelable> assertRoundTrip(
        source: T,
        creator: android.os.Parcelable.Creator<T>,
        assertions: (T) -> Unit
    ) {
        val parcel = Parcel.obtain()
        try {
            source.writeToParcel(parcel, 0)
            parcel.writeInt(SENTINEL)
            parcel.setDataPosition(0)

            assertions(creator.createFromParcel(parcel))
            assertEquals(SENTINEL, parcel.readInt())
            assertEquals(parcel.dataSize(), parcel.dataPosition())
        } finally {
            parcel.recycle()
        }
    }
}
