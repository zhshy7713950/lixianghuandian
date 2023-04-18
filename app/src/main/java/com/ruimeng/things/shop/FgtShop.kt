package   com.ruimeng.things.shop

import android.os.Bundle
import android.view.View
import com.ruimeng.things.R
import wongxd.base.MainTabFragment

/**
 * Created by wongxd on 2018/11/9.
 */
class FgtShop : MainTabFragment() {
    override fun initView(mView: View?, savedInstanceState: Bundle?) {
        initTopbar(mView?.findViewById(R.id.topbar), "商城",false)
    }

    override fun getLayoutRes(): Int = R.layout.fgt_shop

}