package com.ruimeng.things.shop.bean;

import java.io.Serializable;
import java.util.List;


public class DetailBean implements Serializable {

    public String id;
    public String item_id;
    public int cid;
    public String title;
    public String old_price;
    public double end_price;
    public double coupon_price;
    public String coupon_begin;
    public String coupon_end;
    public String sales;
    public String master_image;
    public String recommend_msg;
    public int created;
    public String dtk_id;
    public boolean status;
    public boolean chosen;
    public boolean is_show;
    public String activity_id;
    public int dsr;
    public String app_detail_url;
    public String seller_id;
    public boolean is_tmall;
    public String from_source;
    public boolean is_search;
    public int browse_number;
    public int site_sales;
    public int is_top;
    public List<ImagesBean> images;
    public List<RecommendListBean> recommend_list;
    public String configmsg;
    public String detail_remark;
    public String detail_url;
    public int is_buy;
    public String item_type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOld_price() {
        return old_price;
    }

    public void setOld_price(String old_price) {
        this.old_price = old_price;
    }

    public double getEnd_price() {
        return end_price;
    }

    public void setEnd_price(double end_price) {
        this.end_price = end_price;
    }

    public double getCoupon_price() {
        return coupon_price;
    }

    public void setCoupon_price(double coupon_price) {
        this.coupon_price = coupon_price;
    }

    public String getCoupon_begin() {
        return coupon_begin;
    }

    public void setCoupon_begin(String coupon_begin) {
        this.coupon_begin = coupon_begin;
    }

    public String getCoupon_end() {
        return coupon_end;
    }

    public void setCoupon_end(String coupon_end) {
        this.coupon_end = coupon_end;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getMaster_image() {
        return master_image;
    }

    public void setMaster_image(String master_image) {
        this.master_image = master_image;
    }

    public String getRecommend_msg() {
        return recommend_msg;
    }

    public void setRecommend_msg(String recommend_msg) {
        this.recommend_msg = recommend_msg;
    }

    public int getCreated() {
        return created;
    }

    public void setCreated(int created) {
        this.created = created;
    }

    public String getDtk_id() {
        return dtk_id;
    }

    public void setDtk_id(String dtk_id) {
        this.dtk_id = dtk_id;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setChosen(boolean chosen) {
        this.chosen = chosen;
    }

    public boolean isIs_show() {
        return is_show;
    }

    public void setIs_show(boolean is_show) {
        this.is_show = is_show;
    }

    public String getActivity_id() {
        return activity_id;
    }

    public void setActivity_id(String activity_id) {
        this.activity_id = activity_id;
    }

    public int getDsr() {
        return dsr;
    }

    public void setDsr(int dsr) {
        this.dsr = dsr;
    }

    public String getApp_detail_url() {
        return app_detail_url;
    }

    public void setApp_detail_url(String app_detail_url) {
        this.app_detail_url = app_detail_url;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }

    public boolean isIs_tmall() {
        return is_tmall;
    }

    public void setIs_tmall(boolean is_tmall) {
        this.is_tmall = is_tmall;
    }

    public String getFrom_source() {
        return from_source;
    }

    public void setFrom_source(String from_source) {
        this.from_source = from_source;
    }

    public boolean isIs_search() {
        return is_search;
    }

    public void setIs_search(boolean is_search) {
        this.is_search = is_search;
    }

    public int getBrowse_number() {
        return browse_number;
    }

    public void setBrowse_number(int browse_number) {
        this.browse_number = browse_number;
    }

    public int getSite_sales() {
        return site_sales;
    }

    public void setSite_sales(int site_sales) {
        this.site_sales = site_sales;
    }

    public int getIs_top() {
        return is_top;
    }

    public void setIs_top(int is_top) {
        this.is_top = is_top;
    }

    public List<ImagesBean> getImages() {
        return images;
    }

    public void setImages(List<ImagesBean> images) {
        this.images = images;
    }

    public List<RecommendListBean> getRecommend_list() {
        return recommend_list;
    }

    public void setRecommend_list(List<RecommendListBean> recommend_list) {
        this.recommend_list = recommend_list;
    }

    public String getConfigmsg() {
        return configmsg;
    }

    public void setConfigmsg(String configmsg) {
        this.configmsg = configmsg;
    }

    public String getDetail_remark() {
        return detail_remark;
    }

    public void setDetail_remark(String detail_remark) {
        this.detail_remark = detail_remark;
    }

    public String getDetail_url() {
        return detail_url;
    }

    public void setDetail_url(String detail_url) {
        this.detail_url = detail_url;
    }

    public int getIs_buy() {
        return is_buy;
    }

    public void setIs_buy(int is_buy) {
        this.is_buy = is_buy;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }
}
