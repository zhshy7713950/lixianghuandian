package com.ruimeng.things.shop.fgt;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.GoodsDetailActivity;
import com.ruimeng.things.shop.GoodsListActivity;
import com.ruimeng.things.shop.RecommendAdapter;
import com.ruimeng.things.shop.TKHttpKt;
import com.ruimeng.things.shop.TkPath;
import com.ruimeng.things.shop.adapter.CommonAdapter;
import com.ruimeng.things.shop.adapter.ViewHolder;
import com.ruimeng.things.shop.bean.BannerBean;
import com.ruimeng.things.shop.bean.DiyBtnGroupBean;
import com.ruimeng.things.shop.bean.GetMainBean;
import com.ruimeng.things.shop.bean.ItemsBean;
import com.ruimeng.things.shop.bean.MainBannerBean;
import com.ruimeng.things.shop.bean.NewResultsBean;
import com.ruimeng.things.shop.bean.PopBean;
import com.ruimeng.things.shop.bean.TopBtnBean;
import com.ruimeng.things.shop.view.CustomDialog;
import com.ruimeng.things.shop.view.MyGridView;
import com.ruimeng.things.shop.view.MyListView;
import com.ruimeng.things.shop.view.MyViewPagerBanners;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import wongxd.AtyWeb;
import wongxd.base.custom.MyText;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.net.netDSL.RequestWrapper;
import wongxd.utils.utilcode.util.ScreenUtils;

public class JingDongFragment extends BaseTaoKeFragment implements View.OnClickListener {

    private SmartRefreshLayout refreshLayout;
    private int pageIndex = 1;
    private boolean isLoadMore = true;

    private MyViewPagerBanners banners;
    private List<BannerBean> bannerList = new ArrayList<>();

    private MyText ledText;

    private MyGridView btnView;
    private CommonAdapter<TopBtnBean> btnAdapter;
    private List<TopBtnBean> btnList = new ArrayList<>();

    private MyListView listView;
    private CommonAdapter<MainBannerBean> listAdapter;
    private List<MainBannerBean> listData = new ArrayList<>();

    private LinearLayout diy_btn_group;
    private ImageView group_one, group_two, group_three, group_four,
            group_five, group_six, group_seven;
    private List<DiyBtnGroupBean> groupList = new ArrayList<>();

    private MyGridView itemsView;
    private CommonAdapter<ItemsBean> itemsAdapter;
    private List<ItemsBean> itemsList = new ArrayList<>();

    private RecyclerView recommendView;
    private RecommendAdapter recommendAdapter;
    private List<ItemsBean> recommendList = new ArrayList<>();

    private CustomDialog adDialog;

    private ScrollView scrollView;
    private ImageView back_top;

    @Override
    public void onActivityFragmentResult(String data) {

    }

    @Override
    public int getResouceLayoutId() {
        return R.layout.fgt_jd;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initView() {
        refreshLayout = findView(R.id.account_details_refresh_layout);

        scrollView = findView(R.id.tao_shop_scroll);

        banners = findView(R.id.tao_shop_banner);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(banners.getLayoutParams());
        layoutParams.width = ScreenUtils.getScreenWidth();
        layoutParams.height = ScreenUtils.getScreenWidth() / 2;// GlobalData.getInstance().screenData.getScreenHeight() / 4;
        banners.setLayoutParams(layoutParams);

        ledText = findView(R.id.tao_shop_led);

        btnView = findView(R.id.tao_shop_btn);
        btnView.setFocusable(false);

        listView = findView(R.id.tao_shop_list);
        listView.setFocusable(false);

        diy_btn_group = findView(R.id.tao_shop_diy_btn_group);
        group_one = findView(R.id.tao_shop_group_one);
        group_two = findView(R.id.tao_shop_group_two);
        group_three = findView(R.id.tao_shop_group_three);
        group_four = findView(R.id.tao_shop_group_four);
        group_five = findView(R.id.tao_shop_group_five);
        group_six = findView(R.id.tao_shop_group_six);
        group_seven = findView(R.id.tao_shop_group_seven);

        itemsView = findView(R.id.tao_shop_items);
        itemsView.setFocusable(false);

        recommendView = findView(R.id.tao_shop_recommend);
        recommendView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recommendView.setFocusable(false);
        back_top = findView(R.id.tao_shop_back_top);


        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > 600) {
                    back_top.setVisibility(View.VISIBLE);
                } else {
                    back_top.setVisibility(View.GONE);
                }
            }
        });
        group_one.setOnClickListener(this);
        group_two.setOnClickListener(this);
        group_three.setOnClickListener(this);
        group_four.setOnClickListener(this);
        group_five.setOnClickListener(this);
        group_six.setOnClickListener(this);
        group_seven.setOnClickListener(this);
        back_top.setOnClickListener(this);
    }

    @Override
    public void initData() {
        initBtnAdapter();
        initListAdapter();
        initItemsAdapter();
        initAdapter();
        initRefresh();
    }

    @Override
    public void onClick(View view) {
        if (view == group_one) {
            setGroupListener(0);
        }
        if (view == group_two) {
            setGroupListener(1);
        }
        if (view == group_three) {
            setGroupListener(2);
        }
        if (view == group_four) {
            setGroupListener(3);
        }
        if (view == group_five) {
            setGroupListener(4);
        }
        if (view == group_six) {
            setGroupListener(5);
        }
        if (view == group_seven) {
            setGroupListener(6);
        }
        if (view == back_top) {
            scrollView.fullScroll(View.FOCUS_UP);// 类似于手动拖回顶部,有滚动过程;
        }
    }

    private void initRefresh() {
        refreshLayout.autoRefresh();
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                pageIndex = 2;
                requestGetMain();
                requestPop();
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                if (isLoadMore) {
                    pageIndex++;
                    requestData("jd", "", "asc");
                    isLoadMore = false;
                }
            }
        });
    }


    private void setGroupListener(int position) {
        if (null != groupList && groupList.size() > 0) {
            try {
                String getTitle = groupList.get(position).getLabel();
                String click_class = groupList.get(position).getClick_class();
                String click_param = groupList.get(position).getClick_param();
//                Log.i("data===", "===click_class===" + click_class);
//                Log.i("data===", "===click_param===" + click_param);
                Intent intent;
                if (click_class.equals("view")) {
                    if (click_param.startsWith("itemlist_class")) {
                        //跳转到指定分类产品列表
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("cid", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemdetail")) {
                        //跳转到产品详情
                        intent = new Intent(getActivity(), GoodsDetailActivity.class);
                        intent.putExtra("item_id", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemlist_tag")) {
                        //跳转到指定tag
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("tag", click_param.split("#")[1]);
                        startActivity(intent);
                    }
                } else if (click_class.equals("url") || click_class.equals("webview")) {
                    intent = new Intent(getActivity(), AtyWeb.class);
                    intent.putExtra("url", click_param);
                    startActivity(intent);
                } else if (click_class.equals("systemurl")) {
                    //用系统浏览器打开
                    intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(click_param);
                    intent.setData(content_url);
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setLedData(String msg) {
        ledText.setText(msg);
        ledText.init(getActivity().getWindowManager());
        ledText.startScroll();
    }

    private void initBtnAdapter() {
        btnAdapter = new CommonAdapter<TopBtnBean>(getActivity(), btnList, R.layout.item_btn, true) {

            @Override
            public void convert(ViewHolder helper, TopBtnBean item) {

            }

            @Override
            public void convert(final ViewHolder helper, final TopBtnBean item, int position) {
                super.convert(helper, item, position);
                helper.setImageResource(getActivity(), R.id.item_btn_image, item.getBtn_img());
                helper.setText(R.id.item_btn_name, item.getBtn_label());

            }
        };

        btnView.setAdapter(btnAdapter);

        btnView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String getTitle = btnList.get(position).getBtn_label();
                String click_class = btnList.get(position).getClick_class();
                String click_param = btnList.get(position).getClick_param();
//                Log.i("data===", "===click_class===" + click_class);
//                Log.i("data===", "===click_param===" + click_param);
                Intent intent;
                if (click_class.equals("view")) {
                    if (click_param.startsWith("itemlist_class")) {
                        //跳转到指定分类产品列表
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("cid", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemdetail")) {
                        //跳转到产品详情
                        intent = new Intent(getActivity(), GoodsDetailActivity.class);
                        intent.putExtra("item_id", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemlist_tag")) {
                        //跳转到指定tag
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("tag", click_param.split("#")[1]);
                        startActivity(intent);
                    }
                } else if (click_class.equals("url") || click_class.equals("webview")) {
                    intent = new Intent(getActivity(), AtyWeb.class);
                    intent.putExtra("url", click_param);
                    startActivity(intent);
                } else if (click_class.equals("systemurl")) {
                    //用系统浏览器打开
                    intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(click_param);
                    intent.setData(content_url);
                    startActivity(intent);
                }
            }
        });

    }

    private void initListAdapter() {
        listAdapter = new CommonAdapter<MainBannerBean>(getActivity(), listData, R.layout.item_list, true) {

            @Override
            public void convert(ViewHolder helper, MainBannerBean item) {

            }

            @Override
            public void convert(final ViewHolder helper, final MainBannerBean item, int position) {
                super.convert(helper, item, position);
//                Log.i("data===", "===item.getImg()1===" + item.getImg());
                helper.setImageResource(getActivity(), R.id.item_list_image, item.getImg());

            }
        };

        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String getTitle = listData.get(position).getLabel();
                String click_class = listData.get(position).getClick_class();
                String click_param = listData.get(position).getClick_param();
//                Log.i("data===", "===click_class===" + click_class);
//                Log.i("data===", "===click_param===" + click_param);
                Intent intent;
                if (click_class.equals("view")) {
                    if (click_param.startsWith("itemlist_class")) {
                        //跳转到指定分类产品列表
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("cid", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemdetail")) {
                        //跳转到产品详情
                        intent = new Intent(getActivity(), GoodsDetailActivity.class);
                        intent.putExtra("item_id", click_param.split("#")[1]);
                        startActivity(intent);
                    } else if (click_param.startsWith("itemlist_tag")) {
                        //跳转到指定tag
                        intent = new Intent(getActivity(), GoodsListActivity.class);
                        intent.putExtra("title", getTitle);
                        intent.putExtra("tag", click_param.split("#")[1]);
                        startActivity(intent);
                    }
                } else if (click_class.equals("url") || click_class.equals("webview")) {
                    intent = new Intent(getActivity(), AtyWeb.class);
                    intent.putExtra("url", click_param);
                    startActivity(intent);
                } else if (click_class.equals("systemurl")) {
                    //用系统浏览器打开
                    intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    Uri content_url = Uri.parse(click_param);
                    intent.setData(content_url);
                    startActivity(intent);
                }
            }
        });

    }


    private void initItemsAdapter() {
        itemsAdapter = new CommonAdapter<ItemsBean>(getActivity(), itemsList, R.layout.item_goods_item, true) {

            @Override
            public void convert(ViewHolder helper, ItemsBean item) {

            }

            @Override
            public void convert(final ViewHolder helper, final ItemsBean item, int position) {
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

        itemsView.setAdapter(itemsAdapter);

        itemsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getActivity(), GoodsDetailActivity.class);
                intent.putExtra("item_id", itemsList.get(position).getItem_id());
                startActivity(intent);
            }
        });

    }


    private void initAdapter() {

        recommendAdapter = new RecommendAdapter(recommendList);
        recommendView.setAdapter(recommendAdapter);
        recommendAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(getActivity(), GoodsDetailActivity.class);
                intent.putExtra("item_id", recommendList.get(position).getItem_id());
                startActivity(intent);
            }
        });

    }


    /**
     * 获取主页模块数据
     */
    private void requestGetMain() {

        final Map<String, String> map = new HashMap<>();
        map.put("tag", "jd");

        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getGET_MAIN());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {

                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            GetMainBean getMainBean = AnyKt.getGson().fromJson(data, GetMainBean.class);

                            fillDataWithGetMainBean(getMainBean);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }


                });


                requestWrapper.onFail(new Function2<Integer, String, Unit>() {
                    @Override
                    public Unit invoke(Integer integer, String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });


                requestWrapper.onFinish(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        refreshLayout.finishRefresh();
                        return null;
                    }
                });

                return null;
            }
        });
    }

    private void fillDataWithGetMainBean(GetMainBean getMainBean) {

        bannerList.clear();
        bannerList.addAll(getMainBean.getBanner());
        List<String> mImageUrlList = new ArrayList<>();
        if (null != bannerList && bannerList.size() > 0) {
            for (BannerBean banner : bannerList) {
                mImageUrlList.add(banner.getPic());
            }
        }
//                                            Log.i("data===", "===bannerList.size()===" + bannerList.size());
        if (mImageUrlList.size() != 0 && mImageUrlList != null) {
            banners.setImageResources(mImageUrlList, new MyViewPagerBanners.PagerBannersListener() {
                @Override
                public void onImageClick(int position, View imageView) {
                    String getTitle = bannerList.get(position).getClick_class();
                    String click_class = bannerList.get(position).getClick_class();
                    String click_param = bannerList.get(position).getClick_param();
//                                                        Log.i("data===", "===click_class===" + click_class);
//                                                        Log.i("data===", "===click_param===" + click_param);
                    Intent intent;
                    if (click_class.equals("view")) {
                        if (click_param.startsWith("itemlist_class")) {
                            //跳转到指定分类产品列表
                            intent = new Intent(getActivity(), GoodsListActivity.class);
                            intent.putExtra("title", getTitle);
                            intent.putExtra("cid", click_param.split("#")[1]);
                            startActivity(intent);
                        } else if (click_param.startsWith("itemdetail")) {
                            //跳转到产品详情
                            intent = new Intent(getActivity(), GoodsDetailActivity.class);
                            intent.putExtra("item_id", click_param.split("#")[1]);
                            startActivity(intent);
                        } else if (click_param.startsWith("itemlist_tag")) {
                            //跳转到指定tag
                            intent = new Intent(getActivity(), GoodsListActivity.class);
                            intent.putExtra("title", getTitle);
                            intent.putExtra("tag", click_param.split("#")[1]);
                            startActivity(intent);
                        }
                    } else if (click_class.equals("url") || click_class.equals("webview")) {
                        intent = new Intent(getActivity(), AtyWeb.class);
                        intent.putExtra("url", click_param);
                        startActivity(intent);
                    } else if (click_class.equals("systemurl")) {
                        //用系统浏览器打开
                        intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(click_param);
                        intent.setData(content_url);
                        startActivity(intent);
                    }
                }
            });
        }

        if (!TextUtils.isEmpty(getMainBean.getLed())) {
            setLedData(getMainBean.getLed());
        }

        btnList.clear();
        if (null != getMainBean.getTop_btn() && getMainBean.getTop_btn().size() > 0) {
            btnList.addAll(getMainBean.getTop_btn());
            btnAdapter.notifyDataSetChanged();
        }
//                                            Log.i("data===", "===btnList.size()===" + btnList.size());

        listData.clear();
        if (null != getMainBean.getMain_banner() && getMainBean.getMain_banner().size() > 0) {
            listView.setVisibility(View.VISIBLE);
            listData.addAll(getMainBean.getMain_banner());
            listAdapter.notifyDataSetChanged();
        } else {
            listView.setVisibility(View.GONE);
        }
//                                            Log.i("data===", "===listData.size()===" + listData.size());

        groupList.clear();
        if (null != getMainBean.getDiy_btn_group() && getMainBean.getDiy_btn_group().size() > 0) {
            diy_btn_group.setVisibility(View.VISIBLE);
            groupList.addAll(getMainBean.getDiy_btn_group());
            try {
                Glide.with(getActivity()).load(groupList.get(0).getImg())
                        .into(group_one);
                Glide.with(getActivity()).load(groupList.get(1).getImg())
                        .into(group_two);
                Glide.with(getActivity()).load(groupList.get(2).getImg())
                        .into(group_three);
                Glide.with(getActivity()).load(groupList.get(3).getImg())
                        .into(group_four);
                Glide.with(getActivity()).load(groupList.get(4).getImg())
                        .into(group_five);
                Glide.with(getActivity()).load(groupList.get(5).getImg())
                        .into(group_six);
                Glide.with(getActivity()).load(groupList.get(6).getImg())
                        .into(group_seven);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            diy_btn_group.setVisibility(View.GONE);
        }

        itemsList.clear();
        if (null != getMainBean.getItems() && getMainBean.getItems().size() > 0) {
            itemsList.addAll(getMainBean.getItems());
            itemsAdapter.notifyDataSetChanged();
        }
//                                            Log.i("data===", "===itemsList.size()===" + itemsList.size());

        recommendList.clear();
        if (null != getMainBean.getRecommend_items() && getMainBean.getRecommend_items().size() > 0) {
            recommendList.addAll(getMainBean.getRecommend_items());
            recommendAdapter.notifyDataSetChanged();
        }
//                                            Log.i("data===", "===recommendList.size()===" + recommendList.size());
    }



    /**
     * 产品接口
     *
     * @param tag            今日上新=today,超值9块9=9k9,小编精选=chosen，人气商品=popular，品牌精选=brand
     * @param sort_field     排序字段 price价格，sales销量，discount折扣
     * @param sort_direction asc降序 desc升序
     */
    private void requestData(String tag, String sort_field, String sort_direction) {

        final HashMap<String, String> map = new HashMap();
        map.put("q", "");
        map.put("sort_field", sort_field);
        map.put("sort_direction", sort_direction);
        map.put("cid", "0");
        map.put("pagesize", "20");
        map.put("page", String.valueOf(pageIndex));
        map.put("tag", tag);


        TKHttpKt.tkHttp(new Function1<RequestWrapper, Unit>() {
            @Override
            public Unit invoke(RequestWrapper requestWrapper) {

                requestWrapper.setUrl(TkPath.INSTANCE.getPODUCT());
                requestWrapper.setParams(map);

                requestWrapper.onSuccess(new Function1<String, Unit>() {
                    @Override
                    public Unit invoke(String s) {


                        try {
                            JSONObject json = new JSONObject(s);
                            String data = json.optString("data");
                            NewResultsBean newShopBean = AnyKt.getGson().fromJson(data, NewResultsBean.class);

                            String record_total = newShopBean.getRecord_total();

                            if (null != newShopBean.getItem()) {
                                itemsList.addAll(newShopBean.getItem());
                                itemsAdapter.notifyDataSetChanged();
                            }
                            Log.i("data===", "===itemsList.size()===" + itemsList.size());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });


                requestWrapper.onFail(new Function2<Integer, String, Unit>() {
                    @Override
                    public Unit invoke(Integer integer, String s) {
                        EasyToast.Companion.getDEFAULT().show(s);
                        return null;
                    }
                });


                requestWrapper.onFinish(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        refreshLayout.finishLoadMore();
                        isLoadMore = true;
                        return null;
                    }
                });

                return null;
            }
        });

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
                                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                // 将文本内容放到系统剪贴板里。
                                cm.setText(popBean.getClipboard());
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        return null;
                    }
                });

                return null;
            }
        });


    }

    private void adDialog(final PopBean popBean) {
        adDialog = new CustomDialog(getActivity(), R.layout.dialog_ad);
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
                            Intent intent = new Intent(getActivity(), AtyWeb.class);
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
                                Intent intent = new Intent(getActivity(), GoodsListActivity.class);
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

            Glide.with(getActivity())
                    .load(popBean.getPic())
//                    .apply(options)
                    .into(one_ad_image);
            adDialog.setOnItemClickListener(R.id.ad_image, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adDialog.dismiss();
                    if (!TextUtils.isEmpty(popBean.getUrl())) {
                        if ("0".equals(popBean.getUrl_mode())) {
                            Intent intent = new Intent(getActivity(), AtyWeb.class);
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
                                Intent intent = new Intent(getActivity(), GoodsListActivity.class);
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
