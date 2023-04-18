@file:Suppress("UNCHECKED_CAST")

package com.base.activity

import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jaeger.library.StatusBarUtil
import com.qmuiteam.qmui.widget.QMUITopBar
import wongxd.R

abstract class BaseActivity : AppCompatActivity() {

    fun mBaseActivity() = mBaseActivity!!

    var mBaseActivity: BaseActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBaseActivity = this
        if (getLayoutResource() != 0) {
            setContentView(getLayoutResource())
//            setTitleLayoutColor(this, ColorHelper.getColor(this, R.color.app_color))
            initView()
            setListener()
            initData()
        }
    }

    protected open fun initTopbar(topBar: QMUITopBar, title: String?) {
        topBar.addLeftBackImageButton()
            .setOnClickListener { finish() }
        topBar.setTitle(title)
        topBar.setBackgroundColor(resources.getColor(R.color.app_color))
    }

    protected open fun initTopbar(
        topBar: QMUITopBar,
        title: String?,
        isShowBackBtn: Boolean
    ) {
        if (isShowBackBtn) {
            topBar.addLeftBackImageButton()
                .setOnClickListener { finish() }
        }
        topBar.setTitle(title)
        @Suppress("DEPRECATION")
        topBar.setBackgroundColor(resources.getColor(R.color.app_color))
    }

    protected abstract fun getLayoutResource(): Int

    protected abstract fun initView()

    protected abstract fun setListener()

    protected abstract fun initData()

    @Suppress("UNCHECKED_CAST")
    protected fun <V> findView(id: Int): V {
        return this.findViewById<View>(id) as V
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <V> findView(view: View?, id: Int): V {
        return view!!.findViewById<View>(id) as V
    }

    protected fun setTitleLayoutColor(activity: AppCompatActivity?, @ColorInt color: Int) {
        StatusBarUtil.setColor(activity!!, color, 0)
    }

    protected fun setTitleLayoutImage(view: View?, activity: AppCompatActivity?, @DrawableRes id: Int) {
        view?.background = ContextCompat.getDrawable(activity!!, id)
        com.utils.StatusBarUtil.setTransparentForWindow(this)
    }

    protected fun loadRootFragment(@IdRes containerId: Int, @NonNull toFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(containerId, toFragment)
        transaction.commit()
    }


}