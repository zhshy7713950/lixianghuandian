package   com.ruimeng.things.shop


import org.greenrobot.eventbus.EventBus
import com.ruimeng.things.common.getJsonOrNull
import com.ruimeng.things.shop.bean.TkConfigBean
import wongxd.Config
import wongxd.TkInitEvent
import wongxd.common.EasyToast
import wongxd.common.gson
import wongxd.common.toPOJO

/**
 * Created by wongxd on 2018/11/7.
 */

object TkAny {

    private const val STORE_KEY = "TkConfigBean"

    var tkConfigBean: TkConfigBean.Data
        get() {
            return Config.getDefault().stringCacheUtils.getJsonOrNull(
                STORE_KEY,
                TkConfigBean.Data::class.java
            ) ?: TkConfigBean.Data()

        }
        set(value) {
            TkHttp.token = value.token
            Config.getDefault().stringCacheUtils.put(STORE_KEY, gson.toJson(value))
            EventBus.getDefault().post(TkInitEvent())
        }
}

fun tkLogin(userName: String, unionId: String) {

    tkHttp {
        url = TkPath.tkLogin
        params["n_username"] = "xll_app_$userName"
        params["n_imei"] = unionId

        onSuccess {
            val result = it.toPOJO<TkConfigBean>().data
            TkAny.tkConfigBean = result
            Config.getDefault().stringCacheUtils.put("TkConfigBean", gson.toJson(result))
            EventBus.getDefault().post(TkInitEvent())
        }

        onFail { code, msg ->
            EasyToast.DEFAULT.show(msg)
        }
    }
}
