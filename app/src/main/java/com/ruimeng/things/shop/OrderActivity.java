package com.ruimeng.things.shop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.reflect.TypeToken;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.bean.OderListBean;
import com.ruimeng.things.shop.view.CustomProgressDialog;
import com.ruimeng.things.shop.view.RefreshListView;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.json.JSONObject;
import wongxd.base.BaseBackActivity;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.net.netDSL.RequestWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class OrderActivity extends BaseBackActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, RefreshListView.OnRefreshListener {


    private Activity context;
    private SwipeRefreshLayout refresh;
    private RefreshListView shop_lv;
    private OderAdapter oderAdapter;
    private int pageIndex = 1;
    private List<OderListBean> oderListBeens = new ArrayList<>();
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    oderListBeens.clear();
                    pageIndex = 1;
                    refresh.setRefreshing(false);
                    initData(pageIndex);
                    if (oderAdapter != null) {
                        oderAdapter.notifyDataSetChanged();
                    }
                    break;
            }
        }
    };
    private CustomProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_order);
        dialog = CustomProgressDialog.createDialog(this);
        dialog.show();
        initView();
        initData(pageIndex);
        setListener();
    }

    private void initView() {

        QMUITopBar topBar = findViewById(R.id.topbar);
        initTopbar(topBar,"我的订单");
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh);
        shop_lv = (RefreshListView) findViewById(R.id.shop_lv);

    }

    private void setListener() {
        refresh.setOnRefreshListener(this);
        shop_lv.setOnRefreshListener(this);
        shop_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OderListBean itemBean = oderListBeens.get(position);
                String item_id = itemBean.getItem_id();
                Intent intent = new Intent(context, GoodsDetailActivity.class);
                intent.putExtra("item_id", item_id);
                startActivity(intent);
            }
        });

    }

    private void initData(int page) {
        final Map<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("pagesize", "20");


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getORDERLIST());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            List<OderListBean> newShopBean = AnyKt.getGson().fromJson(data, new TypeToken<List<OderListBean>>() {
                            }.getType());

                            oderListBeens.clear();
                            oderListBeens.addAll(newShopBean);
                            oderAdapter = new OderAdapter(context, oderListBeens, R.layout.newshopoder_item);
                            shop_lv.setAdapter(oderAdapter);
                            if (oderListBeens.size() > 0 && oderListBeens.size() % 20 == 0) {
                                shop_lv.onRefreshComplete(true);
                            } else {
                                shop_lv.onRefreshComplete(false);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });


                requestWrapper.onFail(new Function2<Integer,String, Unit>() {
                    @Override
                    public Unit invoke(Integer i,String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });


                requestWrapper.onFinish(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        dialog.dismiss();
                        return null;
                    }
                });

                return null;
            }
        });


    }

    @Override
    public void onClick(View view) {

    }

    public static boolean checkPackage(Context context, String packageName) {
        if (packageName == null || "".equals(packageName))
            return false;
        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager
                    .GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

    }

    @Override
    public void onRefresh() {
        mHandler.sendEmptyMessageDelayed(1, 1600);
    }

    @Override
    public void moreLoadingListener() {
        pageIndex++;
        lodeData(pageIndex);
    }

    private void lodeData(int page) {
        final Map<String, String> map = new HashMap<>();
        map.put("page", page + "");
        map.put("pagesize", "20");


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getORDERLIST());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            List<OderListBean> newShopBean = AnyKt.getGson().fromJson(data, new TypeToken<List<OderListBean>>() {
                            }.getType());


                            oderListBeens.addAll(newShopBean);
                            oderAdapter.notifyDataSetChanged();
                            if (oderListBeens.size() > 0 && oderListBeens.size() % 20 == 0) {
                                shop_lv.onRefreshComplete(true);
                            } else {
                                shop_lv.onRefreshComplete(false);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });


                requestWrapper.onFail(new Function2<Integer,String, Unit>() {
                    @Override
                    public Unit invoke(Integer i,String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });


                requestWrapper.onFinish(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        dialog.dismiss();
                        return null;
                    }
                });

                return null;
            }
        });


    }
}
