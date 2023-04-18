
import android.widget.Toast
import wongxd.base.BaseBackFragment


/**
 * 双击退出app 的fgt
 */
abstract class BaseMainFragment : BaseBackFragment() {
    private var TOUCH_TIME: Long = 0

    /**
     * 处理回退事件
     *
     * @return
     */
    override fun onBackPressedSupport(): Boolean {
        if (System.currentTimeMillis() - TOUCH_TIME < WAIT_TIME) {
            _mActivity.finish()
        } else {
            TOUCH_TIME = System.currentTimeMillis()
            Toast.makeText(_mActivity, "再次点击后退，退出APP", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    companion object {
        // 再点一次退出程序时间设置
        private val WAIT_TIME = 1500L

    }


}
