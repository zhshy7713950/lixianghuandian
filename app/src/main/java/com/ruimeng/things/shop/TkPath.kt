package   com.ruimeng.things.shop

/**
 * Created by wongxd on 2018/11/21.
 */
object TkPath {

    var tkHost = "http://ntstk.xianlubang.com/"

    /**
     * 【淘客】-客户端初始化并登录获取配置数据
     */
    val tkLogin = "/api/getconfig"

    /**
     * 【淘客】-获取主页模块数据
     */
    val GET_MAIN = "/api/getmainv2"


    /**
     * 【淘客】-弹窗广告
     */
    val POP = "/api/pop"


    /**
     * 【淘客】产品接口
     */
    val PODUCT = "/api/items"


    /**
     * 【淘客】-产品详情
     */
    val DETAIL = "/api/detailv2"


    /**
     * 【淘客】-产品弹幕
     */
    val DYNAMIC_MSG = "/api/dynamicmsg"


    /**
     * 请求兑换码
     */
    val DUIHUANCODE = "/api/exchange"


    /**
     * 【淘客】-分享产品
     */
    val GET_SHARE = "/api/getshare"


    /**
     * 【淘客】-意见反馈
     */
    val FEED_BACK = "/api/feedback"

    /**
     * 【淘客】-图片上传
     */
    val FILE = "/appweb/upload"


    /**
     * 订单列表
     */
    val ORDERLIST = "/api/orders"


    /**
     * 浏览记录
     */
    val VIEW_LOG = "/api/getbrowse "
}