package com.net.call

import com.entity.local.RentStep1Local
import com.entity.remote.RentStep1Remote
import com.net.Server

object BizService {

    suspend fun rentStep1(rentStep1Local: RentStep1Local) = Server.call<RentStep1Local,RentStep1Remote>(
        Api.Rent_Step_1,
        rentStep1Local
    )

}