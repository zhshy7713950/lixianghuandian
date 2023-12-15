package com.xianglilai.lixianghuandian.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import wongxd.common.AnyKt;


/**
 * Created by wongxd on 2018/10/30.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {

    private static String TAG = "WXPayEntryActivity";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (WXEntryActivity.wxApi == null) {
            //  通过WXAPIFactory工厂，获取IWXAPI的实例
            WXEntryActivity.wxApi = WXAPIFactory.createWXAPI(this, WXEntryActivity.APP_ID, true);
        }

        if (!WXEntryActivity.isRegister) {
            // 将该app注册到微信
            WXEntryActivity.wxApi.registerApp(WXEntryActivity.APP_ID);
            WXEntryActivity.isRegister = true;
        }

        //注意：
        //第三方开发者如果使用透明界面来实现WXEntryActivity，需要判断handleIntent的返回值，如果返回值为false，
        // 则说明入参不合法未被SDK处理，应finish当前透明界面，避免外部通过传递非法参数的Intent导致停留在透明界面，引起用户的疑惑
        try {
            WXEntryActivity.wxApi.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        setIntent(intent);
        WXEntryActivity.wxApi.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        Log.d(TAG, "onReq");
        Toast.makeText(this, "微信请求BaseReq.getType = " + req.getType(), Toast.LENGTH_SHORT).show();
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
//                goToGetMsg();
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
//                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            default:
                break;
        }
    }


    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    //app发送消息给微信，处理返回消息的回调
    @Override
    public void onResp(BaseResp resp) {

        Log.d(TAG, "onResp  errcode  " + resp.errCode + "  type  " + resp.getType());

        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
            AnyKt.I(TAG, "onPayFinish,errCode=" + resp.errCode);

            // 0 成功
            // -1  错误
            //-2 用户取消
            String tips = "";
            SweetAlertDialog dlg = null;
            switch (resp.errCode) {
                case 0:
                    tips = "成功";
                    dlg = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                    break;
                case -1:
                    tips = "支付失败";
                    dlg = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                    break;
                case -2:
                    tips = "用户取消支付";
                    dlg = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                    break;
                default:
                    tips = "未知错误";
                    dlg = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                    break;
            }

            final String finalTips = tips;
            dlg.setCancelable(false);
            dlg.setCanceledOnTouchOutside(false);
            dlg.setTitleText(tips)
                    .setConfirmText("确认")
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (WXEntryActivity.callback != null) {
                                if (sweetAlertDialog.getAlerType() == SweetAlertDialog.SUCCESS_TYPE) {
                                    WXEntryActivity.callback.onsuccess("", finalTips);
                                } else {
                                    WXEntryActivity.callback.onFail(finalTips);
                                }
                            }
                            finish();
                        }
                    })
                    .show();

        }

    }

}

