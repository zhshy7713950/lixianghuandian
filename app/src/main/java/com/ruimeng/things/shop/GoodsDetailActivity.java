package com.ruimeng.things.shop;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.adapter.CommonAdapter;
import com.ruimeng.things.shop.adapter.ViewHolder;
import com.ruimeng.things.shop.bean.*;
import com.ruimeng.things.shop.view.CustomDialog;
import com.ruimeng.things.shop.view.MyGridView;
import com.ruimeng.things.shop.view.MyViewPagerBanners;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import org.json.JSONObject;
import wongxd.AtyWeb;
import wongxd.base.BaseBackActivity;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.net.netDSL.RequestWrapper;
import wongxd.utils.utilcode.util.ScreenUtils;

import java.text.SimpleDateFormat;
import java.util.*;


public class GoodsDetailActivity extends BaseBackActivity implements View.OnClickListener {

    private Activity context;
    public String getItemId = "";

    private LinearLayout back_layout;

    private MyViewPagerBanners banners;
    private List<String> bannerList = new ArrayList<>();
    private ImageView goods_type;
    private TextView goods_title, new_price, old_price, coupon;
    private LinearLayout coupon_layout;

    private ViewFlipper flipper_layout;
    private List<DynamicMsgBean> testList = new ArrayList<>();
    private int count;

    private TextView look_layout;

    private MyGridView likeView;
    private CommonAdapter<RecommendListBean> likeAdapter;
    private List<RecommendListBean> likeList = new ArrayList<>();

    private LinearLayout feed_back, goods_share, goods_exchange;

    private DetailBean detailBean;

    private CustomDialog exchangeDialog;
    private boolean isShare = false;

    private CustomDialog adDialog;

    private int isBuy = 2;

    private TextView exchange_coupon, coupon_time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_goods_detail);
        initView();
        setListener();
        initData();

    }

    private void initView() {
        back_layout = findViewById(R.id.goods_detail_back_layout);

        banners = findViewById(R.id.goods_detail_banner);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(banners.getLayoutParams());
        layoutParams.width = ScreenUtils.getScreenWidth();
        layoutParams.height = ScreenUtils.getScreenWidth();
        banners.setLayoutParams(layoutParams);

        flipper_layout = findViewById(R.id.goods_detail_flipper_layout);
        flipper_layout.getBackground().setAlpha(100);//0~255透明度值
        goods_type = findViewById(R.id.goods_detail_goods_type);
        goods_title = findViewById(R.id.goods_detail_goods_title);
        new_price = findViewById(R.id.goods_detail_new_price);
        old_price = findViewById(R.id.goods_detail_old_price);
        coupon = findViewById(R.id.goods_detail_coupon);
        coupon_layout = findViewById(R.id.goods_detail_coupon_layout);

        look_layout = findViewById(R.id.goods_detail_look_layout);

        likeView = findViewById(R.id.goods_detail_like);
        likeView.setFocusable(false);

        feed_back = findViewById(R.id.goods_detail_feed_back);
        goods_share = findViewById(R.id.goods_detail_goods_share);
        goods_exchange = findViewById(R.id.goods_detail_goods_exchange);

        exchange_coupon = findViewById(R.id.goods_detail_exchange_coupon);
        coupon_time = findViewById(R.id.goods_detail_coupon_time);
    }

    private void setListener() {
        back_layout.setOnClickListener(this);
        look_layout.setOnClickListener(this);
        feed_back.setOnClickListener(this);
        goods_share.setOnClickListener(this);
        goods_exchange.setOnClickListener(this);
        exchange_coupon.setOnClickListener(this);
    }

    private void initData() {
        getItemId = getIntent().getStringExtra("item_id");
        initLikeAdapter();
        requestDetail(getItemId);
        requestDynamicMsg(getItemId);
        requestPop();

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view == back_layout) {
            finish();
        }
        //查看宝贝详情
        if (view == look_layout) {
            try {
                if (!TextUtils.isEmpty(detailBean.getApp_detail_url())) {
                    String url = detailBean.getApp_detail_url();
                    if (!AnyKt.openInApp(url, context)) {
                        intent = new Intent(context, AtyWeb.class);
                        intent.putExtra("title", "");
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //反馈
        if (view == feed_back) {
            intent = new Intent(context, FeedBackActivity.class);
            intent.putExtra("item_id", getItemId);
            startActivity(intent);
        }
        //分享商品
        if (view == goods_share) {
            try {
                if (null != detailBean.getConfigmsg()) {
                    isShare = true;

                    if (0 == isBuy) {
                        exchangeDialog(detailBean.getConfigmsg());
                    } else if (1 == isBuy) {
                        iniDuiHuan(getItemId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //立即兑换
        if (view == goods_exchange || view == exchange_coupon) {
            try {
                if (!TextUtils.isEmpty(detailBean.getConfigmsg())) {
                    isShare = false;
                    if (0 == isBuy) {
                        exchangeDialog(detailBean.getConfigmsg());
                    } else if (1 == isBuy) {
                        iniDuiHuan(getItemId);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initLikeAdapter() {
        likeAdapter = new CommonAdapter<RecommendListBean>(context, likeList, R.layout.item_goods_item, true) {

            @Override
            public void convert(ViewHolder helper, RecommendListBean item) {

            }

            @Override
            public void convert(final ViewHolder helper, final RecommendListBean item, int position) {
                super.convert(helper, item, position);
                Glide.with(mContext)
                        .load(item.getMaster_image())
                        .into((ImageView) helper.getView(R.id.goods_item_image));
                ImageView goods_type = helper.getView(R.id.goods_item_goods_type);
                //0 淘宝 1天猫 2京东
                if ("0".equals(item.getItem_type())) {
                    goods_type.setBackgroundResource(R.drawable.goods_taobao);
                } else if ("1".equals(item.getItem_type())) {
                    goods_type.setBackgroundResource(R.drawable.goods_tianmao);
                } else if ("2".equals(item.getItem_type())) {
                    goods_type.setBackgroundResource(R.drawable.goods_jingdong);
                } else if ("3".equals(item.getItem_type())) {
                    goods_type.setBackgroundResource(R.drawable.goods_pinduoduo);
                }
                helper.setText(R.id.goods_item_goods_name, item.getTitle());
                TextView oldprice = helper.getView(R.id.goods_item_old_price);
                oldprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG); // 设置中划线并加清晰
                oldprice.setText("¥" + item.getOld_price());
                helper.setText(R.id.goods_item_saled_number, item.getSales() + "人付款");
                helper.setText(R.id.goods_item_new_price, "券后¥" + item.getEnd_price());
                helper.setText(R.id.goods_item_coupons_price, "可抵扣" + item.getCoupon_price() + "元");

            }
        };

        likeView.setAdapter(likeAdapter);

        likeView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(context, GoodsDetailActivity.class);
                intent.putExtra("item_id", likeList.get(position).getItem_id());
                startActivity(intent);
            }
        });

    }

    private void setBannerData(List<String> list) {
        banners.setImageResources(list, new MyViewPagerBanners.PagerBannersListener() {
            @Override
            public void onImageClick(int position, View imageView) {

            }
        });
    }


    /**
     * 产品详情
     *
     * @param item_id
     */
    private void requestDetail(String item_id) {
        final Map<String, String> map = new HashMap<>();
        map.put("item_id", item_id);


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getDETAIL());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            DetailBean detailBean = AnyKt.getGson().fromJson(data, DetailBean.class);


                            fillDetailData(detailBean);


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

                        return null;
                    }
                });

                return null;
            }
        });


    }

    private void fillDetailData(DetailBean detailBean) {
        this.detailBean = detailBean;
        try {
            bannerList.clear();
            if (null != detailBean.getImages() && detailBean.getImages().size() > 0) {
                List<String> dataList = new ArrayList<>();
                dataList.clear();

                for (int i = 0; i < detailBean.getImages().size(); i++) {
                    dataList.add(detailBean.getImages().get(i).getImg());
                }

                bannerList.addAll(dataList);
                setBannerData(bannerList);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            String priceName = "";
            if ("0".equals(detailBean.getItem_type())) {
                goods_type.setBackgroundResource(R.drawable.goods_taobao);
                priceName = "淘宝价：";
            } else if ("1".equals(detailBean.getItem_type())) {
                goods_type.setBackgroundResource(R.drawable.goods_tianmao);
                priceName = "天猫价：";
            } else if ("2".equals(detailBean.getItem_type())) {
                goods_type.setBackgroundResource(R.drawable.goods_jingdong);
                priceName = "京东价：";
            } else if ("3".equals(detailBean.getItem_type())) {
                goods_type.setBackgroundResource(R.drawable.goods_pinduoduo);
                priceName = "拼多多价：";
            }
            goods_title.setText(detailBean.getTitle());
            old_price.setText(priceName + detailBean.getOld_price() + "元");
            new_price.setText("¥" + detailBean.getEnd_price());
            coupon.setText(detailBean.getCoupon_price() + "元抵用券");
            coupon_time.setText("有效期至" + detailBean.getCoupon_end());
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            likeList.clear();
            if (null != detailBean.getRecommend_list() && detailBean.getRecommend_list().size() > 0) {
                likeList.addAll(detailBean.getRecommend_list());
                likeAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            isBuy = detailBean.getIs_buy();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 产品弹幕
     *
     * @param item_id
     */
    private void requestDynamicMsg(String item_id) {
        final Map<String, String> map = new HashMap<>();
        map.put("item_id", item_id);


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getDYNAMIC_MSG());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            List<DynamicMsgBean> dynamicMsgBeanList = AnyKt.getGson().fromJson(data, new TypeToken<List<DynamicMsgBean>>() {
                            }.getType());

                            testList.addAll(dynamicMsgBeanList);
                            List<String> dataList = new ArrayList();
                            if (null != testList && testList.size() > 0) {
                                flipper_layout.setVisibility(View.GONE);
                                dataList.clear();
                                for (int i = 0; i < testList.size(); i++) {
                                    String str = "用户：" + testList.get(i).getUsername().toString() +
                                            "  " +
                                            times(testList.get(i).getCreated().toString()) + "购买了此商品";
//                                                    Log.i("data===","======"+testList.get(i).getUsername().toString());
//                                                    Log.i("data===","======"+testList.get(i).getCreated().toString());
//                                                    Log.i("data===","======"+str);
                                    dataList.add(i, str);
                                }
                                count = testList.size();
                                for (int i = 0; i < count; i++) {
                                    final View ll_content = View.inflate(context, R.layout.item_flipper, null);
                                    TextView tv_content = (TextView) ll_content.findViewById(R.id.tv_content);
                                    tv_content.setText(dataList.get(i).toString());
                                    flipper_layout.addView(ll_content);
                                }
                            } else {
                                flipper_layout.setVisibility(View.GONE);
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

                        return null;
                    }
                });

                return null;
            }
        });


    }


    private void iniDuiHuan(String item_id) {

        final Map<String, String> map = new HashMap<>();
        map.put("item_id", item_id);


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getDUIHUANCODE());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            DuihuanBean duihuanBean = AnyKt.getGson().fromJson(data, DuihuanBean.class);


                            if (!isShare) {

                                Intent intent;
                                if (duihuanBean != null) {
                                    if (duihuanBean.getClipboard() != null) {
                                        copy(duihuanBean.getClipboard());
                                    }
                                    if (!AnyKt.openInApp(duihuanBean.getCoupon_url(), context)) {
                                        intent = new Intent(context, AtyWeb.class);
                                        intent.putExtra("title", "兑换商品");
                                        intent.putExtra("url", duihuanBean.getCoupon_url()
                                                .replace("tbopen://", "http://"));
                                        startActivity(intent);
                                    }
                                }
                            } else {
                                if (duihuanBean != null) {
                                    requestGetShare(getItemId, duihuanBean.getCoupon_url2());
                                }
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

                        return null;
                    }
                });

                return null;
            }
        });


    }

    /**
     * 分享产品
     *
     * @param item_id
     * @param tburl
     */
    private void requestGetShare(String item_id, String tburl) {
        final Map<String, String> map = new HashMap<>();
        map.put("item_id", item_id);
        map.put("tburl", tburl);


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getGET_SHARE());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            GetShareBean bean = AnyKt.getGson().fromJson(data, GetShareBean.class);
                            Intent intent = new Intent(context, CreateShareActivity.class);
                            intent.putExtra("info", bean);
                            startActivity(intent);


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

                        return null;
                    }
                });

                return null;
            }
        });


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

    private void exchangeDialog(String content) {
        exchangeDialog = new CustomDialog(context, R.layout.dialog_exchange);
        exchangeDialog.setGravity(Gravity.CENTER);
        exchangeDialog.show();
        exchangeDialog.setText(R.id.exchange_content, content);
        exchangeDialog.setOnItemClickListener(R.id.exchange_cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeDialog.dismiss();
            }
        });
        exchangeDialog.setOnItemClickListener(R.id.exchange_confirm, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exchangeDialog.dismiss();
                iniDuiHuan(getItemId);
            }
        });


    }

    public String times(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("HH:mm:ss");//yyyy年MM月dd日HH时mm分ss秒
        @SuppressWarnings("unused")
        long lcc = Long.valueOf(time);
        int i = Integer.parseInt(time);
        String times = sdr.format(new Date(i * 1000L));
        return times;
    }

    private void copy(String s) {
        // 从API11开始android推荐使用android.content.ClipboardManager
        // 为了兼容低版本我们这里使用旧版的android.text.ClipboardManager，虽然提示deprecated，但不影响使用。
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 将文本内容放到系统剪贴板里。
        cm.setText(s);
//        Toast.makeText(context, "复制成功！", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        requestDetail(getItemId);
    }

    /**
     * 弹窗广告
     */
    private void requestPop() {


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getPOP());

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            PopBean popBean = AnyKt.getGson().fromJson(data, PopBean.class);

                            adDialog(popBean);

                            if (!TextUtils.isEmpty(popBean.getClipboard())) {
                                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                // 将文本内容放到系统剪贴板里。
                                cm.setText(popBean.getClipboard());
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

                        return null;
                    }
                });

                return null;
            }
        });


    }


    private void adDialog(final PopBean popBean) {
        adDialog = new CustomDialog(context, R.layout.dialog_ad);
        adDialog.setGravity(Gravity.CENTER);
        adDialog.show();
        ImageView one_ad_image = (ImageView) adDialog.getView(R.id.ad_image);
        LinearLayout one_ad_text = (LinearLayout) adDialog.getView(R.id.ad_text);
        LinearLayout image_layout = (LinearLayout) adDialog.getView(R.id.ad_image_layout);
        LinearLayout ad_root = (LinearLayout) adDialog.getView(R.id.ad_root);
        adDialog.setText(R.id.ad_content, popBean.getContent());
        if ("0".equals(popBean.getPop_mode())) {
            image_layout.setVisibility(View.GONE);
            one_ad_text.setVisibility(View.VISIBLE);

            adDialog.setOnItemClickListener(R.id.ad_cancel, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adDialog.dismiss();
                }
            });
            adDialog.setOnItemClickListener(R.id.ad_confirm, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adDialog.dismiss();
                    if (!TextUtils.isEmpty(popBean.getUrl())) {
                        if ("0".equals(popBean.getUrl_mode())) {
                            Intent intent = new Intent(context, AtyWeb.class);
                            intent.putExtra("url", popBean.getUrl());
                            startActivity(intent);
                        } else if ("1".equals(popBean.getUrl_mode())) {
                            //用系统浏览器打开
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(popBean.getUrl());
                            intent.setData(content_url);
                            startActivity(intent);
                        } else if ("2".equals(popBean.getUrl_mode())) {
                            if (popBean.getClick_param().startsWith("itemlist_tag")) {
                                //跳转到指定tag
                                Intent intent = new Intent(context, GoodsListActivity.class);
                                intent.putExtra("title", popBean.getLabel());
                                intent.putExtra("tag", popBean.getClick_param().split("#")[1]);
                                startActivity(intent);
                            }
                        }
                    }

                }
            });
        } else {
            image_layout.setVisibility(View.VISIBLE);
            image_layout.getBackground().setAlpha(0);//0~255透明度值
//            ad_root.getBackground().setAlpha(0);
            one_ad_text.setVisibility(View.GONE);
//            RequestOptions options = new RequestOptions();
//            options.centerCrop()
//                    .placeholder(R.mipmap.icon_head_bg)
//                    .error(R.mipmap.icon_head_bg)
//                    .transform(new GlideCircleTransform(getActivity()));

            Glide.with(context)
                    .load(popBean.getPic())
//                    .apply(options)
                    .into(one_ad_image);
            adDialog.setOnItemClickListener(R.id.ad_image, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adDialog.dismiss();
                    if (!TextUtils.isEmpty(popBean.getUrl())) {
                        if ("0".equals(popBean.getUrl_mode())) {
                            Intent intent = new Intent(context, AtyWeb.class);
                            intent.putExtra("url", popBean.getUrl());
                            startActivity(intent);
                        } else if ("1".equals(popBean.getUrl_mode())) {
                            //用系统浏览器打开
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(popBean.getUrl());
                            intent.setData(content_url);
                            startActivity(intent);
                        } else if ("2".equals(popBean.getUrl_mode())) {
                            if (popBean.getClick_param().startsWith("itemlist_tag")) {
                                //跳转到指定tag
                                Intent intent = new Intent(context, GoodsListActivity.class);
                                intent.putExtra("title", popBean.getLabel());
                                intent.putExtra("tag", popBean.getClick_param().split("#")[1]);
                                startActivity(intent);
                            }
                        }
                    }
                }
            });
            adDialog.setOnItemClickListener(R.id.ad_close, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adDialog.dismiss();

                }
            });
        }


    }

}
