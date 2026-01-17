package com.net.call

import com.entity.local.AdPayLocal
import com.entity.local.AgentByCodeLocal
import com.entity.local.ChangeErrorLocal
import com.entity.local.GetAdInfoLocal
import com.entity.local.GetCaptchaLocal
import com.entity.local.CheckCaptchaLocal
import com.entity.local.GetCityInfoLocal
import com.entity.local.GetCodeLocal
import com.entity.local.GetMapKeyLocal
import com.entity.local.GetThirdAdStatusLocal
import com.entity.local.OneDeviceLocal
import com.entity.local.OneKeyLoginLocal
import com.entity.local.RentStep1Local
import com.entity.local.ServerPayResultLocal
import com.entity.local.UnregisterLocal
import com.entity.local.UploadVersionLocal
import com.entity.local.UserPaymentInfoLocal
import com.entity.local.VoiceOpenLocal
import com.entity.local.GetCustomerServicePhonesLocal
import com.entity.local.WithdrawListLocal
import com.entity.remote.CustomerServiceContactRemote
import com.entity.remote.AdInfoRemote
import com.entity.remote.AgentInfoRemote
import com.entity.remote.GetCityInfoRemote
import com.entity.remote.GetThirdAdStatusRemote
import com.entity.remote.LoginRemote
import com.entity.remote.RentStep1Remote
import com.entity.remote.ServerPayResultRemote
import com.entity.remote.UserPaymentInfoRemote
import com.net.Server
import com.ruimeng.things.Path
import com.ruimeng.things.home.bean.DeviceDetailBean
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.MyDevicesBean
import com.ruimeng.things.me.bean.WithdrawListResponse

object BizService {

    suspend fun rentStep1(rentStep1Local: RentStep1Local) = Server.call<RentStep1Local,RentStep1Remote>(
        Api.Rent_Step_1,
        rentStep1Local
    )

    suspend fun getAgentByCode(queryAgentByCode: AgentByCodeLocal) = Server.call<AgentByCodeLocal,AgentInfoRemote>(
        Api.Get_Agent_By_Code,
        queryAgentByCode
    )

    suspend fun getUserPaymentInfo(userPaymentInfoLocal: UserPaymentInfoLocal) = Server.call<UserPaymentInfoLocal,UserPaymentInfoRemote>(
        Api.Get_User_Payment_Info,
        userPaymentInfoLocal
    )

    suspend fun getAMapKey(getMapKeyLocal: GetMapKeyLocal) = Server.call<GetMapKeyLocal,String>(
        Api.Get_Map_Key,
        getMapKeyLocal
    )

    suspend fun oneKeyLogin(oneKeyLoginLocal: OneKeyLoginLocal,isShowMsg: Boolean) = Server.call<OneKeyLoginLocal,LoginRemote>(
        Api.One_Key_Login,
        oneKeyLoginLocal,
        isShowMsg = isShowMsg
    )

    suspend fun getAdInfo(getAdInfoLocal: GetAdInfoLocal) = Server.call<GetAdInfoLocal,AdInfoRemote>(
        Api.Get_Advertisement_Info,
        getAdInfoLocal
    )

    suspend fun adPay(adPayLocal: AdPayLocal) = Server.call<AdPayLocal,GetRentPayBean.PayData>(
        Api.Ad_Pay,
        adPayLocal
    )

    suspend fun serverPayResult(serverPayResultLocal: ServerPayResultLocal) = Server.call<ServerPayResultLocal,ServerPayResultRemote>(
        Path.ORDERSTATUS,
        serverPayResultLocal
    )

    suspend fun getMyDevice() = Server.call<Any,List<MyDevicesBean.Data>>(
        Path.GET_MY_DEVICE,
        null
    )

    suspend fun changeError(changeErrorLocal: ChangeErrorLocal) = Server.call<ChangeErrorLocal,Any>(
        Api.Change_Error,
        changeErrorLocal
    )

    suspend fun uploadVersion(uploadVersionLocal: UploadVersionLocal) = Server.call<UploadVersionLocal,Any>(
        Api.Upload_Version,
        uploadVersionLocal
    )

    suspend fun getCode(getCodeLocal: GetCodeLocal) = Server.call<GetCodeLocal,Any>(
        Api.Get_Code,
        getCodeLocal
    )

    suspend fun unregister(unregisterLocal: UnregisterLocal) = Server.call<UnregisterLocal,Any>(
        Api.Unregister,
        unregisterLocal
    )

    suspend fun getCityInfo(getCityInfoLocal: GetCityInfoLocal) = Server.call<GetCityInfoLocal, GetCityInfoRemote>(
        Api.Get_City_Info,
        getCityInfoLocal
    )

    suspend fun getOneDevice(oneDeviceLocal: OneDeviceLocal) = Server.call<OneDeviceLocal, DeviceDetailBean.Data>(
        Api.Get_One_Device,
        oneDeviceLocal
    )

    suspend fun getCaptcha(getCaptchaLocal: GetCaptchaLocal) = Server.call<GetCaptchaLocal, String>(
        Api.Get_Captcha,
        getCaptchaLocal
    )

    suspend fun checkCaptcha(checkCaptchaLocal: CheckCaptchaLocal) = Server.call<CheckCaptchaLocal, Any>(
        Api.Check_Captcha,
        checkCaptchaLocal
    )

    suspend fun getThirdAdStatus() = Server.call<GetThirdAdStatusLocal, GetThirdAdStatusRemote>(
        Api.Get_Third_Ad_Status,
        GetThirdAdStatusLocal()
    )

    suspend fun voiceOpen(voiceOpenLocal: VoiceOpenLocal) = Server.call<VoiceOpenLocal, Any>(
        Api.Voice_Open,
        voiceOpenLocal
    )

    // 获取区域客服电话（对象列表：时间段、联系人、号码）
    suspend fun getCustomerServicePhones(local: GetCustomerServicePhonesLocal) = Server.call<GetCustomerServicePhonesLocal, List<CustomerServiceContactRemote>>(
        Api.Get_Customer_Service_Phones,
        local
    )

    suspend fun getWithdrawList(local: WithdrawListLocal) = Server.call<WithdrawListLocal, WithdrawListResponse>(
        Api.Distribute_Withdraw_List,
        local
    )
}