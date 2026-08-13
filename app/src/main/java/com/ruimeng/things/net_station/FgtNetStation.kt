package com.ruimeng.things.net_station

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import com.ruimeng.things.R
import com.ruimeng.things.net_station.net_city_data.CityDataWorker
import kotlinx.android.synthetic.main.fgt_net_station.*
import kotlinx.coroutines.launch
import me.yokeyword.fragmentation.SupportFragment
import wongxd.base.BaseBackFragment
import wongxd.common.getSweetDialog
import wongxd.common.permission.PermissionType
import wongxd.common.permission.getPermissions
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by wongxd on 2019/7/3.
 */
class FgtNetStation : BaseBackFragment() {
    override fun getLayoutRes(): Int = R.layout.fgt_net_station

    override fun onLazyInitView(savedInstanceState: Bundle?) {
        super.onLazyInitView(savedInstanceState)
        initTopbar(topbar, "网点列表", true)
        topbar.addRightTextButton("刷新", R.id.right).apply {
            setTextColor(Color.WHITE)
            setOnClickListener {
                fragmentList[0].refresh()
            }
        }
        requestNetWorkShow()
    }

    private var fragmentList = ArrayList<FgtNetStationItem>()
    private fun requestNetWorkShow() {
        fragmentList.add(FgtNetStationItem.newInstance())
        setView(fragmentList.toTypedArray())
    }

    private fun setView(list: Array<SupportFragment>) {
        showHideFragment(
            fragmentList[0]
        )
        val dlg = getSweetDialog(SweetAlertDialog.PROGRESS_TYPE, "定位中", true)
        dlg.show()

        val lifecycleOwner = viewLifecycleOwner
        val dismissOnDestroy = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY && dlg.isShowing) {
                dlg.dismiss()
            }
        }
        lifecycleOwner.lifecycle.addObserver(dismissOnDestroy)

        val requestStarted = AtomicBoolean(false)
        val resolveLocationAndLoadStations: () -> Unit = resolve@{
            if (!requestStarted.compareAndSet(false, true)) return@resolve

            lifecycleOwner.lifecycleScope.launch {
                try {
                    LocationUtil.resolveLocation(requireContext())
                    loadMultipleRootFragment(R.id.fl_net_station, 0, *list)
                } finally {
                    lifecycleOwner.lifecycle.removeObserver(dismissOnDestroy)
                    if (dlg.isShowing) {
                        dlg.dismissWithAnimation()
                    }
                }
            }
        }

        getPermissions(activity,
            PermissionType.COARSE_LOCATION,
            PermissionType.FINE_LOCATION,
            result = { _, _ ->
                resolveLocationAndLoadStations()
            },
            allGranted = {
                resolveLocationAndLoadStations()
            })

        CityDataWorker.initJsonData()
    }


}
