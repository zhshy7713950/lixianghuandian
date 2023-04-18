package com.ruimeng.things.shop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.bean.NewResultBean;
import com.ruimeng.things.shop.decoration.SpacesItemDecoration;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import wongxd.base.BaseBackActivity;
import wongxd.common.AnyKt;
import wongxd.common.EasyToast;
import wongxd.common.net.netDSL.RequestWrapper;


public class GoodsListActivity extends BaseBackActivity implements View.OnClickListener {

    private Activity context;

    private String getTitle = "";
    private String getKeyWords = "", getSortField = "discount",
            getSortDirection = "asc", getTag = "", getCid = "";

    private SmartRefreshLayout refreshLayout;
    private int pageIndex = 1;
    private boolean isLoadMore = true;

    private LinearLayout one_layout, two_layout, three_layout, four_layout;
    private TextView one_text, two_text, three_text, four_text;
    private View one_line, two_line, three_line, four_line;
    private ImageView four_image;
    private boolean isOneUp = true, isTwoUp = false, isThreeUp = false, isFourUp = false;

    private RecyclerView recyclerView;
    private GoodsListAdapter adapter;
    private List<NewResultBean.ItemBean> dataList = new ArrayList<>();

    //    private ScrollView scrollView;
    private ImageView back_top;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_goods_list);
        initView();
        setListener();
        initData();
    }

    private void initView() {


        refreshLayout = findViewById(R.id.goods_list_refresh_layout);

        back_top = findViewById(R.id.goods_list_back_top);

        one_layout = findViewById(R.id.goods_list_screen_one_layout);
        two_layout = findViewById(R.id.goods_list_screen_two_layout);
        three_layout = findViewById(R.id.goods_list_screen_three_layout);
        four_layout = findViewById(R.id.goods_list_screen_four_layout);

        one_text = findViewById(R.id.goods_list_screen_one_text);
        two_text = findViewById(R.id.goods_list_screen_two_text);
        three_text = findViewById(R.id.goods_list_screen_three_text);
        four_text = findViewById(R.id.goods_list_screen_four_text);

        four_image = findViewById(R.id.goods_list_screen_four_image);

        one_line = findViewById(R.id.goods_list_screen_one_line);
        two_line = findViewById(R.id.goods_list_screen_two_line);
        three_line = findViewById(R.id.goods_list_screen_three_line);
        four_line = findViewById(R.id.goods_list_screen_four_line);

        recyclerView = findViewById(R.id.goods_list_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        //添加ItemDecoration，item之间的间隔
        int leftRight = dip2px(5f);
        int topBottom = dip2px(5f);

        recyclerView.addItemDecoration(new SpacesItemDecoration(leftRight, topBottom));

    }

    float density = Resources.getSystem().getDisplayMetrics().density;

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dip2px(float dpValue) {
        return (int) (0.5f + dpValue * density);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public float px2dip(float pxValue) {
        return (pxValue / density);
    }

    @SuppressLint("NewApi")
    private void setListener() {
//        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                if (scrollY > 600) {
//                    back_top.setVisibility(View.VISIBLE);
//                } else {
//                    back_top.setVisibility(View.GONE);
//                }
//            }
//        });

        //设置RecyclerView滑动监听器 addOnScrollListener(),其中setOnScrollListener()方法已过时
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //获得recyclerView的线性布局管理器
                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                //获取到第一个item的显示的下标  不等于0表示第一个item处于不可见状态 说明列表没有滑动到顶部 显示回到顶部按钮
                int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();
                // 当不滚动时
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // 判断是否滚动超过一屏
                    if (firstVisibleItemPosition == 0) {
                        back_top.setVisibility(View.GONE);
                    } else {
                        //显示回到顶部按钮
                        back_top.setVisibility(View.VISIBLE);
                    }
                    //获取RecyclerView滑动时候的状态
                } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {//拖动中
                    back_top.setVisibility(View.GONE);
                }
            }
        });

        back_top.setOnClickListener(this);
        one_layout.setOnClickListener(this);
        two_layout.setOnClickListener(this);
        three_layout.setOnClickListener(this);
        four_layout.setOnClickListener(this);
    }

    private void initData() {

        if (!TextUtils.isEmpty(getIntent().getStringExtra("title"))) {
            getTitle = getIntent().getStringExtra("title");
        }

        if (!TextUtils.isEmpty(getIntent().getStringExtra("keywords"))) {
            getKeyWords = getIntent().getStringExtra("keywords");
        }

        if (!TextUtils.isEmpty(getIntent().getStringExtra("tag"))) {
            getTag = getIntent().getStringExtra("tag");
        }
        if (!TextUtils.isEmpty(getIntent().getStringExtra("cid"))) {
            getCid = getIntent().getStringExtra("cid");
        }
        if (!TextUtils.isEmpty(getIntent().getStringExtra("from"))) {

        }


        QMUITopBar topBar = findViewById(R.id.topbar);
        initTopbar(topBar,getTitle);


        initAdapter();
        initRefresh();
    }

    @Override
    public void onClick(View view) {
        if (view == back_top) {
//            scrollView.fullScroll(View.FOCUS_UP);// 类似于手动拖回顶部,有滚动过程;
            recyclerView.scrollToPosition(0);
            back_top.setVisibility(View.GONE);
        }

        if (view == one_layout) {
            getSortField = "discount";
            if (isOneUp) {
                getSortDirection = "asc";
                isOneUp = false;
            } else {
                getSortDirection = "desc";
                isOneUp = true;
            }
            one_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
            two_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            three_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            four_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));

            four_image.setImageResource(R.drawable.price_normal);

            one_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
            two_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            three_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            four_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

            if (null != refreshLayout) {
                refreshLayout.autoRefresh();
            }
        }
        if (view == two_layout) {
            getSortField = "";
            if (isTwoUp) {
                getSortDirection = "asc";
                isTwoUp = false;
            } else {
                getSortDirection = "desc";
                isTwoUp = true;
            }
            one_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            two_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
            three_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            four_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));

            four_image.setImageResource(R.drawable.price_normal);

            one_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            two_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
            three_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            four_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

            if (null != refreshLayout) {
                refreshLayout.autoRefresh();
            }
        }
        if (view == three_layout) {
            getSortField = "sales";
            if (isThreeUp) {
                getSortDirection = "asc";
                isThreeUp = false;
            } else {
                getSortDirection = "desc";
                isThreeUp = true;
            }
            one_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            two_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            three_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
            four_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));

            four_image.setImageResource(R.drawable.price_normal);

            one_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            two_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            three_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
            four_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));

            if (null != refreshLayout) {
                refreshLayout.autoRefresh();
            }
        }
        if (view == four_layout) {
            getSortField = "price";
            if (isFourUp) {
                getSortDirection = "asc";
                isFourUp = false;
                four_image.setImageResource(R.drawable.price_up);
            } else {
                getSortDirection = "desc";
                isFourUp = true;
                four_image.setImageResource(R.drawable.price_down);
            }
            one_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            two_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            three_text.setTextColor(ContextCompat.getColor(context, R.color.gray_98));
            four_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));

            one_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            two_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            three_line.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            four_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
            if (null != refreshLayout) {
                refreshLayout.autoRefresh();
            }
        }
    }

    private void initRefresh() {
        refreshLayout.autoRefresh();
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                back_top.setVisibility(View.GONE);
                pageIndex = 1;
                dataList.clear();
                adapter.notifyDataSetChanged();
                requestData(getKeyWords, getTag, getSortField, getSortDirection, getCid);
            }
        });

        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                refreshLayout.finishLoadMore();
                if (isLoadMore) {
                    pageIndex++;
                    requestData(getKeyWords, getTag, getSortField, getSortDirection, getCid);
                    isLoadMore = false;
                }
            }
        });


    }

    private void initAdapter() {
        adapter = new GoodsListAdapter(dataList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(context, GoodsDetailActivity.class);
                intent.putExtra("item_id", dataList.get(position).getItem_id());
                startActivity(intent);
            }
        });
    }


    /**
     * 产品接口
     *
     * @param q              搜索参数
     * @param tag            今日上新=today,超值9块9=9k9,小编精选=chosen，人气商品=popular，品牌精选=brand
     * @param sort_field     排序字段 price价格，sales销量，discount折扣
     * @param sort_direction asc降序 desc升序
     */
    private void requestData(String q, String tag, String sort_field, String sort_direction,String cid) {
        final HashMap<String, String> map = new HashMap();
        map.put("q", q);
        map.put("sort_field", sort_field);
        map.put("sort_direction", sort_direction);
        map.put("cid", cid);
        map.put("pagesize", "20");
        map.put("page",String.valueOf(pageIndex));
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
                            NewResultBean newShopBean = AnyKt.getGson().fromJson(data, NewResultBean.class);

                            String record_total = newShopBean.getRecord_total();

                            List<NewResultBean.ItemBean> item = newShopBean.getItem();
                            if (null != newShopBean.getItem() && newShopBean.getItem().size() > 0) {
                                dataList.addAll(newShopBean.getItem());
                            }
                            adapter.notifyDataSetChanged();

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
                        refreshLayout.finishRefresh();
                        refreshLayout.finishLoadMore();
                        return null;
                    }
                });

                return null;
            }
        });

    }


    /**
     * 针对TextView显示中文中出现的排版错乱问题，通过调用此方法得以解决
     *
     * @param str
     * @return 返回全部为全角字符的字符串
     */
    public String toDBC(String str) {
        char[] c = str.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375) {
                c[i] = (char) (c[i] - 65248);
            }

        }
        return new String(c);
    }
}
