package wongxd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import wongxd.base.AppManager;
import wongxd.base.BaseBackActivity;


/**
 * Created by wongxd on 2018/11/5.
 * https://github.com/wongxd
 * wxd1@live.com
 */
public class AtyWeb extends BaseBackActivity {


    private static final String TAG = "WebViewActivity";
    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    private WebView webView;
    private String web_url;//URL
    private String web_title;//标题

    private boolean isOnPause;

    private Context context;

    public static void start(String title, String url) {
        Context ctx = AppManager.getAppManager().currentActivity();
        Intent i = new Intent(ctx, AtyWeb.class);
        i.putExtra(KEY_TITLE, title);
        i.putExtra(KEY_URL, url);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_web);
        web_url = getIntent().getExtras().getString(KEY_URL);
        web_title = getIntent().getExtras().getString(KEY_TITLE);
        setMidTitle(web_title);
        addBackListener();

        if (TextUtils.isEmpty(web_url)) {
//            ToastUtil.show("URL不能为空。");
            this.finish();
            return;
        }
        Log.d(TAG, "url = " + web_url);
        webView = (WebView) findViewById(R.id.brand_layout);

        WebSettings webSettings = webView.getSettings();
        webSettings.setPluginState(WebSettings.PluginState.ON);
        webSettings.setLightTouchEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAppCacheEnabled(true);

        webSettings.setAllowFileAccess(true);
        webSettings.setSavePassword(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        /**
         * 用WebView显示图片，可使用这个参数 设置网页布局类型：
         * 1、LayoutAlgorithm.NARROW_COLUMNS ：适应内容大小
         * 2、LayoutAlgorithm.SINGLE_COLUMN : 适应屏幕，内容将自动缩放
         */
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setHorizontalScrollbarOverlay(true);
        webView.setHorizontalScrollBarEnabled(true);
        webView.setVerticalScrollBarEnabled(true);
        webView.requestFocus();
//		if (Build.VERSION.SDK_INT >= 11) {
//            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        }
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                startBrowser(context, url);
            }
        });
        webView.setWebChromeClient(new MyWebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, final String url) {

                view.loadUrl(url);
                return true;
            }
        });

        if (web_url.contains("http://") || web_url.contains("https://")) {
            webView.loadUrl(web_url);
        }else {
            webView.loadData(web_url,"text/html","utf-8");
        }

//        setLeftCloseButton("关闭", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (webView != null) {
//                    webView.getSettings().setJavaScriptEnabled(false);
//                    webView.getSettings().setBuiltInZoomControls(false);
//                }
//                finish();
//            }
//        });
//        setRightButton("更多", new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showMenu(webView);
//            }
//        });
    }

    protected TextView middleTitle;

    /**
     * 设置AC标题
     *
     * @param string
     */
    protected void setMidTitle(String string) {
        if (middleTitle == null) {
            middleTitle = (TextView) findViewById(R.id.base_title);
        }
        middleTitle.setText(string);
    }

    protected void setMidTitle(String string, float size) {
        if (middleTitle == null) {
            middleTitle = (TextView) findViewById(R.id.base_title);
            middleTitle.setTextSize(size);
        }
        middleTitle.setText(string);
    }


    /**
     * 返回上一层
     */
    protected TextView addBackListener() {
        TextView view = setLeftButton(R.drawable.arrow_right_white, null, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        view.setPadding(6, 0, 0, 0);
        return view;
    }

    float density = Resources.getSystem().getDisplayMetrics().density;


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    private TextView setLeftButton(int bgRes, String string, View.OnClickListener listener) {
        Button leftButton = (Button) findViewById(R.id.left_button);
        if (!TextUtils.isEmpty(string)) {
            leftButton.setText(string);
            leftButton.setTextSize(20);
        }
        if (bgRes != 0) {
            leftButton.setCompoundDrawablesWithIntrinsicBounds(bgRes, 0, 0, 0);
            int width = dip2px(28f);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(leftButton.getLayoutParams());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            layoutParams.width = width;
            leftButton.setLayoutParams(layoutParams);
        }
        if (listener != null) {
            leftButton.setOnClickListener(listener);
        }
        leftButton.setRotation(180);
        leftButton.setVisibility(View.VISIBLE);
        return leftButton;
    }


    protected void setLeftCloseButton(String string, View.OnClickListener listener) {
        Button leftButton = (Button) findViewById(R.id.left_close_button);
        if (!TextUtils.isEmpty(string)) {
            leftButton.setText(string);
            leftButton.setTextSize(15);
        }
        if (listener != null) {
            leftButton.setOnClickListener(listener);
        }
        leftButton.setVisibility(View.VISIBLE);
    }


    protected void setRightButton(String string, View.OnClickListener listener) {
        setRightButton(0, string, listener);
    }

    protected void setRightButton(int bgRes, View.OnClickListener listener) {
        setRightButton(bgRes, null, listener);
    }

    private void setRightButton(int bgRes, String string, View.OnClickListener listener) {
        Button rightButton = (Button) findViewById(R.id.right_title);
        if (!TextUtils.isEmpty(string)) {
            rightButton.setText(string);
        }
        if (bgRes != 0) {
            rightButton.setBackgroundResource(bgRes);
            int width = dip2px(28);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(rightButton.getLayoutParams());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.width = width;
            rightButton.setLayoutParams(layoutParams);
        }
        if (listener != null) {
            rightButton.setOnClickListener(listener);
        }
        rightButton.setVisibility(View.VISIBLE);
    }


    public static void startBrowser(Context context, String url) {
        try {
            Uri uri = Uri.parse(url);
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception e) {
//            ToastUtil.show("没有找到浏览器打开。");
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        try {
            if (isOnPause) {
                if (webView != null) {
                    webView.getClass().getMethod("onResume").invoke(webView, (Object[]) null);
                }
                isOnPause = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        try {
            if (webView != null) {
                webView.getClass().getMethod("onPause").invoke(webView, (Object[]) null);
                isOnPause = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        try {
            if (webView != null) {
                webView.getSettings().setBuiltInZoomControls(false);
                webView.setVisibility(View.GONE);
            }
            isOnPause = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (webView != null) {
            webView.stopLoading();
        }
        super.onStop();
    }

    private void showMenu(final View showAtLocation) {
        final Context context = showAtLocation.getContext();
        View layout = LayoutInflater.from(context).inflate(R.layout.layout_menu_webview, null);
        final PopupWindow menuWindow = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        menuWindow.setFocusable(true);
        menuWindow.update();
        menuWindow.setBackgroundDrawable(context.getResources().getDrawable(R.color.transparent));
        menuWindow.showAtLocation(showAtLocation, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        layout.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuWindow.dismiss();
            }
        });
        layout.findViewById(R.id.toBrowser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBrowser(context, web_url);
            }
        });
        layout.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(web_url);
                menuWindow.dismiss();
            }
        });

    }


    @Override
    public void onBackPressedSupport() {
        super.onBackPressedSupport();
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        showMenu(webView);
        return false;
    }


    FrameLayout video;
    View title_bar;

    private class MyWebChromeClient extends WebChromeClient {
        CustomViewCallback customViewCallback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            customViewCallback = callback;
            // 设置webView隐藏
            webView.setVisibility(View.GONE);
            // 声明video，把之后的视频放到这里面去
            video = (FrameLayout) findViewById(R.id.video);
            // 将video放到当前视图中
            video.addView(view);
            title_bar = findViewById(R.id.title_bar);
            title_bar.setVisibility(View.GONE);
            // 横屏显示
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // 设置全屏
            setFullScreen();
        }

        @Override
        public void onHideCustomView() {
            if (customViewCallback != null) {
                // 隐藏掉
                customViewCallback.onCustomViewHidden();
            }
            // 用户当前的首选方向
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // 退出全屏
            quitFullScreen();
            // 设置WebView可见
            webView.setVisibility(View.VISIBLE);
//            video.removeAllViews();
            title_bar.setVisibility(View.VISIBLE);
        }

        /**
         * 设置全屏
         */
        private void setFullScreen() {
            // 设置全屏的相关属性，获取当前的屏幕状态，然后设置全屏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // 全屏下的状态码：1098974464
            // 窗口下的状态吗：1098973440
        }

        /**
         * 退出全屏
         */
        private void quitFullScreen() {
            // 声明当前屏幕状态的参数并获取
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("网页提示");
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //点击确定按钮之后，继续执行网页中的操作
                    result.confirm();
                }
            });
            builder.setCancelable(false);
            builder.create();
            builder.show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("网页内容");
            builder.setMessage(message);
            builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.confirm();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            });
            builder.setCancelable(false);
            builder.create();
            builder.show();
            return true;
        }

        //处理javascript中的prompt
        //message为网页中对话框的提示内容
        //defaultValue为没有输入时默认显示的内容
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            //自定义一个带输入的对话框由TextView和Edittext构成
            final LayoutInflater factory = LayoutInflater.from(context);
            final View dialogView = factory.inflate(R.layout.webview_prom_dialog, null);
            //设置TextView对应网页中的提示信息
            ((TextView) dialogView.findViewById(R.id.TextView_PROM)).setText(defaultValue);
            //设置EditText对应网页中的输入框
            ((EditText) dialogView.findViewById(R.id.EditText_PROM)).setText(defaultValue);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setTitle("请输入内容");
            builder.setView(dialogView);
            builder.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // 点击确定之后，取得输入的值，传给网页处理
                    String value = ((EditText) dialogView.findViewById(R.id.EditText_PROM)).getText().toString();
                    result.confirm(value);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    result.cancel();
                }
            });
            builder.setOnCancelListener(new AlertDialog.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    result.cancel();
                }
            });
            builder.show();
            return true;
        }

        //设置网页的加载的进度条
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            getWindow().setFeatureInt(Window.FEATURE_PROGRESS, newProgress);
            super.onProgressChanged(view, newProgress);
        }

        //设置应用程序的标题title
        @Override
        public void onReceivedTitle(WebView view, String title) {
//            setMidTitle(title);
            super.onReceivedTitle(view, title);
        }
    }
}
