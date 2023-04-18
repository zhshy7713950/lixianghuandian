package com.ruimeng.things

/**
 * Created by wongxd on 2018/11/13.
 */
object Path {

//    val host = "http://xianglilai.app.xianlubang.com/"

    /**
     * 更新检测
     */
    val CHECK_UPGRADE = "/api/updatecheck"


    /**
     * 微信登陆
     */
    val WX_LOGIN = "/api/wxlogin"


    /**
     * APP配置获取
     */
    val GET_CONFIG = "/api/getconfig"


    /**
     * 获取验证码
     */
    val GET_CODE = "/api/getcode"


    /**
     * 绑定手机号
     */
    val BIND_MOBILE = "/api/bindmobile"


    /**
     * 扫码租车STEP1
     */
    val RENTSTEP1 = "/api/rentstep1"


    /**
     * 获取设备押金信息
     */
    val GET_DEPOSIT = "/api/getdeposit"


    /**
     * 获取押金支付参数
     */
    val GET_PAY_BY_DEPOSIT = "/api/getpaybydepositv2"


    /**
     * 获取设备租用信息
     */
    val GET_RENT = "/api/getrent"


    /**
     * 获取设备租金支付参数
     */
    val GET_RENT_PAY = "/api/getrentpay"


    /**
     * 获取我的设备
     */
    val GET_ONE_DEVICE = "/api/getonedevice"


    /**
     * 获取设备当前坐标(寻车)
     */
    val DEVICE_GEO = "/api/devicegeo"


    /**
     * 开关设备
     */
    val OPT_DEVICE = "/api/optdevice"


    /**
     * 获取我的电池列表(简易)
     */
    val GET_MY_DEVICE = "/api/getmydevice"


    /**
     * 设置电池备注
     */
    val SET_DEVICE_REMARK = "/api/setdeviceremark"


    /**
     * 我的优惠卷
     */
    val GET_MY_COUPON = "/api/getmycoupon"


    /**
     * 租用协议get
     */
    val RENT_AGREEMENT = "/appweb/rentagreement"


    /**
     * 刷新用户信息
     */
    val USERINFO = "/api/userinfo"


    /**
     * 绑定推送
     */
    val BIND_PUSH = "/api/bindpush"


    /**
     * 附近商户
     */
    val LBS_AGENT = "/api/lbsagent"


    /**
     * 解绑电池
     */
    val UNBIND_BATTERY = "/api/unbind"


    /**
     * 消息
     */
    val APPMSG = "/api/appmsg"


    /**
     * 维修申请
     */
    val REPAIR = "/api/repair"

    /**
     * 维修记录
     */
    val REPAIR_LOG = "/api/repairlist"


    /**
     * 通用图片上传
     */
    val UPLOAD_IMG = "/appweb/upload"


    /**
     * 实名认证
     */
    val REALNAME = "/api/setrealname"


    /**
     * 实名认证情况
     */
    val REALNAME_INFO = "/api/realnameinfo"


    /**
     * 获取分享信息
     */
    val SHARE_INFO = "/api/shareinfo"


    /**
     * 申请代理商
     */
    val APPLY_AGENT = "/api/applyagent"


    /**
     * 帮助中心
     */
    val HELP = "/appweb/help"


    /**
     * 关于我们
     */
    val ABOUT_ME = "/appweb/aboutme"


    /**
     * 银行卡绑定查询
     */
    val BANK_QUERY = "/api/bankquery"


    /**
     * 绑定银行卡
     */
    val BIND_BANK = "/api/bindbank"


    /**
     * 解绑银行卡
     */
    val UNBIND_BANK = "/api/unbindbank"


    /**
     * 退还申请
     */
    val RETURN_DEVICE = "/api/returndevice"

    /**
     * 退还记录
     */
    val RETURN_LIST = "/api/returnlist"


    /**
     * 设置支付密码
     */
    val SETPAYPWD = "/api/setpaypwd"

    /**
     * 支付订单状态查询
     */
    val ORDERSTATUS = "/api/orderstatus"


    /**
     * 获取轨迹点
     */
    val LBSHISTORICAL = "/api/lbshistorical"


    /**
     * 查询用户信用数据
     */
    val CREDITINFO = "/api/creditinfo"


    /**
     * 信用合同列表
     */
    val CREDITLIST = "/api/creditlist"


    /**
     * 信用认证
     */
    val CREDITAPPLY = "/api/creditapply"


    /**
     * 信用合同签约数据
     */
    val CREDITCONTRACT = "/api/creditcontract"


    /**
     * 信用合同签约获取验证码
     */
    val CREDITCODE = "/api/creditcode"

    /**
     * 确认签约信用借款合同
     */
    val CREDITSIGN = "/api/creditsign"

    /**
     * 信用合同还款列表
     */
    val REPAYMENTLIST = "/api/repaymentlist"

    /**
     * 获取单期还款信息
     */
    val REPAYMENTINFO = "/api/repaymentinfo"

    /**
     * 获取单期还款支付参数
     */
    val REPAYMENT = "/api/repayment"

    /**
     * 获取首付支付参数
     */
    val GETFIRSTPAYINFO = "/api/getfirstpayinfo"


    /**
     * 上传手持照片+设备照片
     */
    val HANDIMG = "/api/handimg"


    /**
     * 服务网点列表
     */
    val SERVICE_NETWORK = "/api/servicenetwork"


    /**
     * 电池退还站点
     */
    val RETURN_NETWORK = "/api/returnnetwork"


    /**
     * 手机号登录
     */
    val MOBLILE_LOGIN = "/api/mobilelogin"


    /**
     * 套餐组合-合约列表
     */
    val CONTRACT_COMPOSE = "/api/contractcompose"



    /**
     * 套餐组合-新增设备绑定
     */
    val COMPOSE_BIND = "/api/composebind"

}