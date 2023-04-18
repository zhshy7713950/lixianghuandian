package com.ruimeng.things.shop;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruimeng.things.R;
import com.ruimeng.things.shop.bean.SearchBean;
import com.ruimeng.things.shop.view.FluidLayout;
import wongxd.Config;
import wongxd.base.BaseBackActivity;

import java.util.ArrayList;
import java.util.List;


public class NewSearchActivity extends BaseBackActivity implements View.OnClickListener {

    private Activity context;
    private LinearLayout search_layout, del_layout;
    private EditText search_input;
    private TextView search_btn;

    private TextView taobao_text, jingdong_text, pinduoduo_text;
    private View taobao_line, jingdong_line, pinduoduo_line;

    private ImageView clear, clear_s;

    private FluidLayout search_history, search_find;
    private List<SearchBean> historyListData = new ArrayList<>();
    private List<String> hotListData = new ArrayList<>();

    private String getCid = "", getTag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.aty_new_search);
        initView();
        setListener();
        initData();
    }


    private void initView() {
        LinearLayout back_layout = findViewById(R.id.left_back_layout);
        back_layout.setVisibility(View.VISIBLE);
        back_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        search_layout = findViewById(R.id.search_layout);
        search_layout.setVisibility(View.VISIBLE);
        search_input = findViewById(R.id.search_input);
        del_layout = findViewById(R.id.search_del_layout);
        search_btn = findViewById(R.id.search_btn);

        taobao_text = findViewById(R.id.search_taobao_text);
        jingdong_text = findViewById(R.id.search_jingdong_text);
        pinduoduo_text = findViewById(R.id.search_pinduoduo_text);

        taobao_line = findViewById(R.id.search_taobao_line);
        jingdong_line = findViewById(R.id.search_jingdong_line);
        pinduoduo_line = findViewById(R.id.search_pinduoduo_line);

        taobao_text.setOnClickListener(this);
        jingdong_text.setOnClickListener(this);
        pinduoduo_text.setOnClickListener(this);

        clear = findViewById(R.id.new_search_clear);
        clear_s = findViewById(R.id.new_search_clear_s);

        search_history = findViewById(R.id.new_search_history);
        search_find = findViewById(R.id.new_search_find);
    }

    private void setListener() {
        search_btn.setOnClickListener(this);
        del_layout.setOnClickListener(this);

        search_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
//                    search_type.setText("搜索");
                    del_layout.setVisibility(View.VISIBLE);
                } else {
//                    search_type.setText("取消");
                    del_layout.setVisibility(View.INVISIBLE);
                }
            }
        });


        search_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // 当按了搜索之后关闭软键盘
                    ((InputMethodManager) search_input.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                            NewSearchActivity.this.getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    if (!TextUtils.isEmpty(search_input.getText())) {

                        SearchBean bean = new SearchBean();
                        bean.setKeyword(search_input.getText().toString());
                        boolean isAdd = true;
                        if (historyListData.size() > 0) {
                            for (SearchBean searchBean : historyListData) {
                                if (TextUtils.equals(searchBean.getKeyword(), bean.getKeyword())) {
                                    isAdd = false;
                                    break;
                                }
                            }
                        }
                        if (isAdd) {
                            if (!TextUtils.isEmpty(bean.getKeyword())) {
                                historyListData.add(0, bean);
                            }
                        }

                        Config.Companion.getDefault().getSpUtils().put("historySearchData", new Gson().toJson(historyListData));
                    }
                    Intent intent = new Intent(context, GoodsListActivity.class);
                    intent.putExtra("title", search_input.getText().toString());
                    intent.putExtra("keywords", search_input.getText().toString());
                    intent.putExtra("cid", getCid);
                    intent.putExtra("tag", getTag);
                    intent.putExtra("from", "search");
                    startActivity(intent);


                    return true;
                }
                return false;
            }
        });
        clear.setOnClickListener(this);
        clear_s.setOnClickListener(this);
    }

    private void initData() {
        getCid = getIntent().getStringExtra("cid");
        setLine(0);
        addHistoryData();
        addHotData();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        if (view == search_btn) {
            if (!TextUtils.isEmpty(search_input.getText())) {
                SearchBean bean = new SearchBean();
                bean.setKeyword(search_input.getText().toString());
                boolean isAdd = true;
                if (historyListData.size() > 0) {
                    for (SearchBean searchBean : historyListData) {
                        if (TextUtils.equals(searchBean.getKeyword(), bean.getKeyword())) {
                            isAdd = false;
                            break;
                        }
                    }
                }
                if (isAdd) {
                    if (!TextUtils.isEmpty(bean.getKeyword())) {
                        historyListData.add(0, bean);
                    }
                }

                Config.Companion.getDefault().getSpUtils().put("historySearchData", new Gson().toJson(historyListData));
            }
            intent = new Intent(context, GoodsListActivity.class);
            intent.putExtra("title", search_input.getText().toString());
            intent.putExtra("keywords", search_input.getText().toString());
            intent.putExtra("cid", getCid);
            intent.putExtra("tag", getTag);
            intent.putExtra("from", "search");
            startActivity(intent);

        }
        if (view == del_layout) {
            search_input.setText("");
        }

        if (view == taobao_text) {
            getCid = "";
            getTag = "";
            setLine(0);
        }

        if (view == jingdong_text) {
            getCid = "99";
            getTag = "jd";
            setLine(1);
        }

        if (view == pinduoduo_text) {
            getCid = "100";
            getTag = "pdd";
            setLine(2);
        }

        if (view == clear) {
            historyListData.clear();
            Config.Companion.getDefault().getSpUtils().put("historySearchData", new Gson().toJson(historyListData));
            addHistoryData();
        }
        if (view == clear_s) {
            hotListData.clear();
            Config.Companion.getDefault().getSpUtils().put("hot_search", new Gson().toJson(historyListData));
            addHotData();
        }

    }

    private void addHistoryData() {
        String collectContact = Config.Companion.getDefault().getSpUtils().getString("historySearchData", "");
        if (!TextUtils.isEmpty(collectContact)) {
            List<SearchBean> dataList = new Gson().fromJson(collectContact, new TypeToken<List<SearchBean>>() {
            }.getType());
            historyListData.clear();
            historyListData.addAll(dataList);

//            List<SearchBean> listData = new ArrayList<>();
//            listData.clear();
//            if (historyListData.size() > 0 && historyListData != null) {
//                for (int i = 0; i < historyListData.size(); i++) {
//                    SearchBean searchBean = new SearchBean();
//                    searchBean.setId(historyListData.get(i).getId());
//                    searchBean.setKeyword(historyListData.get(i).getKeyword());
//                    searchBean.setNum(historyListData.get(i).getNum());
//                    searchBean.setCreated(historyListData.get(i).getCreated());
//                    listData.add(searchBean);
//                }
//            }

            getHistoryTag(historyListData);
        }
    }

    private void getHistoryTag(final List<SearchBean> list) {
        search_history.removeAllViews();
        search_history.setGravity(Gravity.TOP);
        for (int i = 0; i < list.size(); i++) {

            View view = LayoutInflater.from(context).inflate(R.layout.layout_tag, null);
            TextView tv = (TextView) view.findViewById(R.id.tag_text);
            tv.setText(list.get(i).getKeyword().toString());


//            TextView tv = new TextView(this);
//            tv.setText(list.get(i).getKeyword().toString());
//            tv.setTextSize(12);
//            tv.setTextColor(Color.parseColor("#989898"));
//            tv.setBackgroundResource(R.drawable.fillet_bg_gray_ec);
//            tv.setPadding(15, 5, 15, 5);
//            tv.setGravity(Gravity.CENTER);
//            tv.setMinWidth(70);
//            tv.setMinHeight(50);
            final int position = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(GoodsSearchActivity.this, SearchResultActivity.class);
//                    intent.putExtra("title", list.get(position).getKeyword().toString());
//                    startActivity(intent);
                    Intent intent = new Intent(context, GoodsListActivity.class);
                    intent.putExtra("title", list.get(position).getKeyword().toString());
                    intent.putExtra("keywords", list.get(position).getKeyword().toString());
                    intent.putExtra("cid", getCid);
                    intent.putExtra("tag", getTag);
                    intent.putExtra("from", "search");
                    startActivity(intent);
                }
            });
            FluidLayout.LayoutParams params = new FluidLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 10, 10);
            search_history.addView(view, params);
        }
    }

    private void addHotData() {
        String collectContact = Config.Companion.getDefault().getSpUtils().getString( "hot_search", "");
        if (!TextUtils.isEmpty(collectContact)) {

            String hot[] = collectContact.split(",");
            Log.i("data===", "=====" + hot.toString());
            hotListData.clear();
            for (int i = 0; i < hot.length; i++) {
                if (hot[i].contains("[]")) {
                    break;
                }
                hotListData.add(hot[i]);
            }

            getHotTag(hotListData);
        }
    }

    private void getHotTag(final List<String> list) {
        search_find.removeAllViews();
        search_find.setGravity(Gravity.TOP);
        for (int i = 0; i < list.size(); i++) {

            View view = LayoutInflater.from(context).inflate(R.layout.layout_tag, null);
            TextView tv = (TextView) view.findViewById(R.id.tag_text);
            tv.setText(list.get(i).toString());

//            TextView tv = new TextView(this);
//            tv.setText(list.get(i).toString());
//            tv.setTextSize(12);
//            tv.setTextColor(Color.parseColor("#989898"));
//            tv.setBackgroundResource(R.drawable.fillet_bg_gray_ec);
//            tv.setPadding(15, 5, 15, 5);
//            tv.setGravity(Gravity.CENTER);
//            tv.setMinWidth(70);
//            tv.setMinHeight(50);
            final int position = i;
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Intent intent = new Intent(GoodsSearchActivity.this, SearchResultActivity.class);
//                    intent.putExtra("title", list.get(position).getKeyword().toString());
//                    startActivity(intent);
                    SearchBean bean = new SearchBean();
                    bean.setKeyword(list.get(position).toString());
                    boolean isAdd = true;
                    if (historyListData.size() > 0) {
                        for (SearchBean searchBean : historyListData) {
                            if (TextUtils.equals(searchBean.getKeyword(), bean.getKeyword())) {
                                isAdd = false;
                                break;
                            }
                        }
                    }
                    if (isAdd) {
                        if (!TextUtils.isEmpty(bean.getKeyword())) {
                            historyListData.add(0, bean);
                        }
                    }
                    Config.Companion.getDefault().getSpUtils().put("historySearchData", new Gson().toJson(historyListData));
                    Intent intent = new Intent(context, GoodsListActivity.class);
                    intent.putExtra("title", list.get(position).toString());
                    intent.putExtra("keywords", list.get(position).toString());
                    intent.putExtra("cid", getCid);
                    intent.putExtra("tag", getTag);
                    intent.putExtra("from", "search");
                    startActivity(intent);
                }
            });
            FluidLayout.LayoutParams params = new FluidLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 10, 10);
            search_find.addView(view, params);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        addHistoryData();
    }

    private void setLine(int position) {
        switch (position) {
            default:
                break;
            case 0:
                taobao_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
                jingdong_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                pinduoduo_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                taobao_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
                jingdong_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                pinduoduo_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                break;
            case 1:
                taobao_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                jingdong_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
                pinduoduo_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                taobao_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                jingdong_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
                pinduoduo_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                break;
            case 2:
                taobao_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                jingdong_text.setTextColor(ContextCompat.getColor(context, R.color.white));
                pinduoduo_text.setTextColor(ContextCompat.getColor(context, R.color.search_line_color));
                taobao_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                jingdong_line.setBackgroundColor(ContextCompat.getColor(context, R.color.app_color));
                pinduoduo_line.setBackgroundColor(ContextCompat.getColor(context, R.color.search_line_color));
                break;
        }
    }

}
