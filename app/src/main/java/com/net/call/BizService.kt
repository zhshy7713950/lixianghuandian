package com.net.call

import com.entity.local.AgentByCodeLocal
import com.entity.local.GetMapKeyLocal
import com.entity.local.OneKeyLoginLocal
import com.entity.local.RentStep1Local
import com.entity.local.UserPaymentInfoLocal
import com.entity.remote.AgentInfoRemote
import com.entity.remote.LoginRemote
import com.entity.remote.RentStep1Remote
import com.entity.remote.UserPaymentInfoRemote
import com.net.Server

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

}