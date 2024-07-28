package com.net.call

import com.entity.local.AdPayLocal
import com.entity.local.AgentByCodeLocal
import com.entity.local.GetAdInfoLocal
import com.entity.local.GetMapKeyLocal
import com.entity.local.OneKeyLoginLocal
import com.entity.local.RentStep1Local
import com.entity.local.ServerPayResultLocal
import com.entity.local.UserPaymentInfoLocal
import com.entity.remote.AdInfoRemote
import com.entity.remote.AgentInfoRemote
import com.entity.remote.LoginRemote
import com.entity.remote.RentStep1Remote
import com.entity.remote.ServerPayResultRemote
import com.entity.remote.UserPaymentInfoRemote
import com.net.Server
import com.ruimeng.things.Path
import com.ruimeng.things.home.bean.GetRentPayBean
import com.ruimeng.things.home.bean.MyDevicesBean

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

    suspend fun oneKeyLogin(oneKeyLoginLocal: OneKeyLoginLocal) = Server.call<OneKeyLoginLocal,LoginRemote>(
        Api.One_Key_Login,
        oneKeyLoginLocal
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
}