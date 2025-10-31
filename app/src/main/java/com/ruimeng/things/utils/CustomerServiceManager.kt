package com.ruimeng.things.utils

import com.entity.remote.CustomerServiceContactRemote
import androidx.fragment.app.FragmentActivity
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet
import wongxd.utils.utilcode.util.PhoneUtils

object CustomerServiceManager {
    @Volatile
    private var contacts: List<CustomerServiceContactRemote> = emptyList()

    fun setPhones(phones: List<CustomerServiceContactRemote>) {
        contacts = phones
    }

    fun getContacts(): List<CustomerServiceContactRemote> = contacts

    fun getPrimaryPhone(): String? = contacts.firstOrNull()?.mobile

    fun showDialSheet(activity: FragmentActivity) {
        val builder = QMUIBottomSheet.BottomListSheetBuilder(activity)
        // 总部客服固定项
        val hqDisplay = "总部客服：400-028-3969"
        val hqDial = "4000283969"
        builder.addItem(hqDisplay, "HQ:$hqDial")

        // 区域客服电话项
        contacts.forEach { c ->
            val display = "区域客服：${c.mobile}"
            val dialNum = sanitizeDialNumber(c.mobile)
            builder.addItem(display, "REGION:$dialNum")
        }

        builder.setOnSheetItemClickListener { dialog, _, _, tag ->
            val number = tag.substringAfter(":")
            PhoneUtils.dial(number)
            dialog.dismiss()
        }
        builder.build().show()
    }

    private fun sanitizeDialNumber(input: String): String = input.replace(Regex("[^0-9]"), "")
}