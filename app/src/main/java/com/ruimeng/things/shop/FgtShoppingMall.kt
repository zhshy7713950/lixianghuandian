package   com.ruimeng.things.shop

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.ruimeng.things.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import com.ruimeng.things.shop.bean.TabLayoutBean
import com.ruimeng.things.shop.fgt.JingDongFragment
import com.ruimeng.things.shop.fgt.PinDuoDuoFragment
import com.ruimeng.things.shop.fgt.TaoBaoFragment
import wongxd.TkInitEvent
import wongxd.base.MainTabFragment

class FgtShoppingMall : MainTabFragment() {


    private var tableLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var tablayoutAdapter: TabLayoutAdapter? = null
    private val titleList: MutableList<TabLayoutBean> = mutableListOf()
    private val fragments: MutableList<Fragment> =
        mutableListOf(TaoBaoFragment(), JingDongFragment(), PinDuoDuoFragment())


    private fun setTableLayout() {
        titleList.clear()
        val titles = arrayOf("淘宝", "京东", "拼多多")
        for (i in titles.indices) {
            val tabLayoutBean = TabLayoutBean()
            tabLayoutBean.setName(titles[i])
            titleList.add(tabLayoutBean)
        }


        tablayoutAdapter = TabLayoutAdapter(childFragmentManager, titleList, fragments)
        viewPager?.offscreenPageLimit = 1
        viewPager?.adapter = tablayoutAdapter
        tableLayout!!.setupWithViewPager(viewPager)
        viewPager?.currentItem = 0
        tablayoutAdapter?.notifyDataSetChanged()
    }

    override fun initView(mView: View, savedInstanceState: Bundle?) {

        tableLayout = mView.findViewById(R.id.shopping_mall_tab_layout)
        viewPager = mView.findViewById(R.id.shopping_mall_view_pager)

        val ivSearch = mView.findViewById<ImageView>(R.id.iv_search)
        ivSearch.setOnClickListener {
            val intent = Intent(context, NewSearchActivity::class.java)
            intent.putExtra("cid", "")
            startActivity(intent)
        }


        val ivMyOrder = mView.findViewById<ImageView>(R.id.iv_my_order)

        ivMyOrder.setOnClickListener {
            val intent = Intent(context, OrderActivity::class.java)
            startActivity(intent)
        }



        EventBus.getDefault().register(this)
        initShopInfo(TkInitEvent())
    }

    override fun getLayoutRes(): Int {
        return R.layout.fgt_shopping_mall
    }


    @Subscribe
    fun initShopInfo(event: TkInitEvent) {
        setTableLayout()
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }
}
