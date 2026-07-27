package com.net.call

object Api {

    const val Rent_Step_1 = "apiv4/rentstep1"

    const val Get_Agent_By_Code = "apiv6/cabinet/getagentbycode"

    const val Get_User_Payment_Info = "apiv6/payment/getuserpaymentinfo"

    const val Get_Map_Key = "apiv6/xlluser/getamapkey"

    const val One_Key_Login = "wangyilogin/gettoken"

    const val Get_Advertisement_Info = "apiv6/advertisementinfo/getadvertisement"

    const val Ad_Pay = "apiv6/advertisementinfo/packagepay"

    const val Change_Error = "apiv6/cabinet/changeerror"

    const val Upload_Version = "apiv6/user/uploadversion"

    const val Get_Code = "api/getcode"

    const val Unregister = "apiv6/user/unregister"

    const val Get_City_Info = "apiv6/xlluser/getcityinfo"

    const val Get_One_Device = "apiv4/getonedevice"

    // Login / Captcha
    const val Get_Captcha = "apiv6/user/getcapcha"
    const val Check_Captcha = "apiv6/user/checkcaptcha"
    
    // Advertisement
    const val Get_Third_Ad_Status = "apiv6/advertisementinfo/getthridadstatus"
    /** 获取后管平台当前上架版本号（用于判断应用商店审核中） */
    const val Get_New_App_Ver = "apiv6/message/getnewappver"
    
    // Voice
    const val Voice_Open = "apiv6/user/voiceopen"

    // Customer Service
    const val Get_Customer_Service_Phones = "apiv6/cgstationnetwork/customerservice"

    // Withdraw
    const val Distribute_Withdraw_List = "apiv6/distribute/withdrawlist"
}