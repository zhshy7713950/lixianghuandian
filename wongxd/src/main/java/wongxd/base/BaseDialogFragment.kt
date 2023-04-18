package wongxd.base

import android.graphics.drawable.ColorDrawable
import android.os.Bundle

import android.view.Window
import androidx.fragment.app.DialogFragment
import wongxd.utils.utilcode.util.ScreenUtils

/**
 * Created by wongxd on 2018/11/13.
 */
open class BaseDialogFragment : DialogFragment() {

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onActivityCreated(savedInstanceState)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0x00000000))
        getDialog()?.getWindow()?.setLayout(
            ScreenUtils.getScreenWidth() / 3 * 2
//            WindowManager.LayoutParams.MATCH_PARENT
            ,
//            WindowManager.LayoutParams.MATCH_PARENT
            ScreenUtils.getScreenHeight() / 3 * 2
        )

    }
}