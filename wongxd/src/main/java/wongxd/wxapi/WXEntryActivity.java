package wongxd.wxapi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.MainLooper;


/**
 * Created by wongxd on 2018/10/30.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {


    //更换应用需要修改的部分
    public static final String APP_ID = "wxbc775f5998093d5a";
    public static final String SECRET = "";


    /**
     * ##############################不用修改的部分###################################################
     */

    public interface WxCallback {

        void onsuccess(String code, String msg);

        void onFail(String msg);
    }


    public static void wxLogin(Context ctx, WxCallback callback) {
        WXEntryActivity.isShare = false;
        WXEntryActivity.callback = callback;
        if (wxApi == null) {
            //  通过WXAPIFactory工厂，获取IWXAPI的实例
            wxApi = WXAPIFactory.createWXAPI(ctx, APP_ID, true);
        }

        if (!isRegister) {
            // 将该app注册到微信
            wxApi.registerApp(APP_ID);
            isRegister = true;
        }

        if (!wxApi.isWXAppInstalled()) {
            String tips = "您还未安装微信客户端";
            EasyToast.Companion.getDEFAULT().show(tips);
            callback.onFail(tips);
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_login";
        wxApi.sendReq(req);

    }

    public static class WxPayEntity {

        public String appId = "";
        public String partnerId = "";
        public String prepayId = "";
        public String packageValue = "";
        public String nonceStr = "";
        public String timeStamp = "";
        public String sign = "";

        public WxPayEntity() {
        }
    }

    public static void wxPay(Context ctx, WxPayEntity entity, WxCallback callback) {


        WXEntryActivity.callback = callback;
        if (wxApi == null) {
            //  通过WXAPIFactory工厂，获取IWXAPI的实例
            wxApi = WXAPIFactory.createWXAPI(ctx, APP_ID, true);
        }

        if (!isRegister) {
            // 将该app注册到微信
            wxApi.registerApp(APP_ID);
            isRegister = true;
        }

        if (!wxApi.isWXAppInstalled()) {
            String tips = "您还未安装微信客户端";
            EasyToast.Companion.getDEFAULT().show(tips);
            callback.onFail(tips);
            return;
        }

        PayReq request = new PayReq();
        request.appId = entity.appId;
        request.partnerId = entity.partnerId;
        request.prepayId = entity.prepayId;
        request.packageValue = entity.packageValue;
        request.nonceStr = entity.nonceStr;
        request.timeStamp = entity.timeStamp;
        request.sign = entity.sign;
        wxApi.sendReq(request);
    }


    /**
     * @param ctx
     * @param iMediaObject 微信分享的对象
     * @param title
     * @param des
     * @param thumb        缩略图路径
     * @param isTimeline
     */
    public static void wxShare(Context ctx, WXMediaMessage.IMediaObject iMediaObject, String title, String des, Bitmap thumb, boolean isTimeline) {

        isShare = true;

        WXMediaMessage mediaMessage = new WXMediaMessage();
        mediaMessage.title = title;
        mediaMessage.description = des;
        mediaMessage.mediaObject = iMediaObject;
        if (thumb != null) {
            mediaMessage.setThumbImage(thumb);
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = WXEntryActivity.class.getName();
        req.message = mediaMessage;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;


        if (wxApi == null) {
            //  通过WXAPIFactory工厂，获取IWXAPI的实例
            wxApi = WXAPIFactory.createWXAPI(ctx, APP_ID, true);
        }

        if (!isRegister) {
            // 将该app注册到微信
            wxApi.registerApp(APP_ID);
            isRegister = true;
        }

        if (!wxApi.isWXAppInstalled()) {
            String tips = "您还未安装微信客户端";
            EasyToast.Companion.getDEFAULT().show(tips);
            callback.onFail(tips);
            return;
        }


        wxApi.sendReq(req);


    }

    /**
     * 需要跑在子线程
     * <p>
     * 把网络资源图片转化成bitmap
     *
     * @param url 网络资源图片
     * @return Bitmap
     */
    public static Bitmap getLocalOrNetBitmap(String url) {
        Bitmap bitmap = null;
        InputStream in = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedInputStream(new URL(url).openStream(), 1024);
            final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
            out = new BufferedOutputStream(dataStream, 1024);
            copy(in, out);
            out.flush();
            byte[] data = dataStream.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            data = null;
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream out)
            throws IOException {
        byte[] b = new byte[1024];
        int read;
        while ((read = in.read(b)) != -1) {
            out.write(b, 0, read);
        }
    }


    private static String TAG = "WXEntryActivity";

    private static boolean isRegister = false;

    public static boolean isShare = false;

    // IWXAPI 是第三方app和微信通信的openapi接口
    public static IWXAPI wxApi;

    public static String wxCode;

    public static WxCallback callback;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (wxApi == null) {
            //  通过WXAPIFactory工厂，获取IWXAPI的实例
            wxApi = WXAPIFactory.createWXAPI(this, WXEntryActivity.APP_ID, true);
        }

        if (!isRegister) {
            // 将该app注册到微信
            wxApi.registerApp(APP_ID);
        }

        //注意：
        //第三方开发者如果使用透明界面来实现WXEntryActivity，需要判断handleIntent的返回值，如果返回值为false，
        // 则说明入参不合法未被SDK处理，应finish当前透明界面，避免外部通过传递非法参数的Intent导致停留在透明界面，引起用户的疑惑
        try {
            wxApi.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        wxApi.handleIntent(intent, this);
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
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
        AnyKt.I(TAG, resp.errCode + "  type  " + resp.getType());

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
            dlg.setTitleText(tips)
                    .setConfirmText("确认")
                    .showCancelButton(false)
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            if (callback != null) {
                                if (sweetAlertDialog.getAlerType() == SweetAlertDialog.SUCCESS_TYPE) {
                                    callback.onsuccess("", finalTips);
                                } else {
                                    callback.onFail(finalTips);
                                }
                            }
                            finish();
                        }
                    })
                    .show();

            return;
        }

        String msg = "";
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                if (isShare) {
                    msg = "微信 分享失败!   " + resp.errStr;
                } else {
                    msg = "微信 登录失败!   " + resp.errStr;
                }
                if (callback != null)
                    callback.onFail(msg);
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:

                if (isShare) {
                    msg = "微信 取消分享！";
                } else {
                    msg = "微信 取消登录!";
                }
                break;
            case BaseResp.ErrCode.ERR_OK:
                if (!isShare) {
                    //拿到了微信返回的code,立马再去请求access_token
                    String code = ((SendAuth.Resp) resp).code;
                    //就在这个地方，用网络库什么的或者自己封的网络api，发请求去咯，注意是get请求
                    wxCode = code;
                    if (TextUtils.isEmpty(code)) {
                        msg = "微信 登录CODE 获取失败";
                    } else {
                        msg = "微信 登录CODE 获取成功";
//                        getAccess_token(code);
                        if (callback != null)
                            callback.onsuccess(wxCode, msg);
                    }
                } else {
                    msg = "微信分享成功";
                }
                if (callback != null)
                    callback.onsuccess(null, msg);
                break;

            default:
                msg = "微信 未知错误";
                if (callback != null)
                    callback.onFail(msg);

                break;
        }
        if (!TextUtils.isEmpty(msg))
            EasyToast.Companion.getDEFAULT().show(msg);
        isShare = false;
        finish();
    }


    /**
     * ###################################通过code获取微信用户信息####################################
     */


    //    String code = "";
    //    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=APPID&secret=SECRET&code=CODE&grant_type=authorization_code";

    String startUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID + "&secret=" + SECRET +
            "&code=";
    String endUrl = "&grant_type=authorization_code";

    private ProgressDialog dialog;

    /**
     * 获取openid accessToken值用于后期操作
     *
     * @param code 请求码
     */
    private void getAccess_token(final String code) {

        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.show();


        getReq(startUrl + code + endUrl, new GetCallback() {
            @Override
            public void s(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String openid = jsonObject.optString("openid");
                    String access_token = jsonObject.optString("access_token");
                    if (TextUtils.isEmpty(openid) || TextUtils.isEmpty(access_token)) {
                        EasyToast.Companion.getDEFAULT().show("微信 登录失败");
                    } else {
                        getUserMesg(access_token, openid);
                    }

                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void f(String errmsg) {
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                EasyToast.Companion.getDEFAULT().show("微信登录失败");
                finish();
            }
        });


    }


    /**
     * 获取微信的个人信息
     *
     * @param access_token
     * @param openid
     */
    private void getUserMesg(final String access_token, final String openid) {
        final ProgressDialog dlg = new ProgressDialog(this);
        dlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dlg.setCancelable(false);
        dlg.show();

        String path = "https://api.weixin.qq.com/sns/userinfo?access_token="
                + access_token
                + "&openid="
                + openid;
        Log.i(TAG, "getUserMesg：" + path);


        getReq(path, new GetCallback() {
            @Override
            public void s(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);

                    String nickname = jsonObject.optString("nickname");
                    int sex = jsonObject.optInt("sex"); //普通用户性别，1为男性，2为女性

                    String province = jsonObject.optString("province");
                    String city = jsonObject.optString("city");
                    String country = jsonObject.optString("country");
                    String headimgurl = jsonObject.optString("headimgurl");
                    String unionid = jsonObject.optString("unionid");


                    AnyKt.I(TAG, "用户信息  " + nickname + "  " + city + "  " + "  " + headimgurl);

                    if (dlg.isShowing())
                        dlg.dismiss();
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void f(String errmsg) {
                if (dlg.isShowing())
                    dlg.dismiss();
                EasyToast.Companion.getDEFAULT().show("获取用户信息失败");
                finish();
            }
        });


    }


    interface GetCallback {
        void s(String response);

        void f(String errmsg);
    }

    private void getReq(final String path, final GetCallback callback) {


        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5 * 1000);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    byte[] data = new byte[1024];
                    StringBuffer sb = new StringBuffer();
                    int length = 0;
                    while ((length = inputStream.read(data)) != -1) {
                        String s = new String(data, Charset.forName("utf-8"));
                        sb.append(s);
                    }
                    final String response = sb.toString();
                    inputStream.close();
                    connection.disconnect();
                    MainLooper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.s(response);
                        }
                    });


                } catch (final Exception e) {
                    e.printStackTrace();
                    MainLooper.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.f(e.getMessage());
                        }
                    });

                }

            }
        }).start();

    }
}

