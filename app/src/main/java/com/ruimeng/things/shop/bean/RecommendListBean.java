package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class RecommendListBean implements Serializable {

    public String item_id;
    public String title;
    public String old_price;
    public String end_price;
    public String coupon_price;
    public String coupon_begin;
    public String coupon_end;
    public String sales;
    public String master_image;
    private String item_type;

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
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

    public String getEnd_price() {
        return end_price;
    }

    public void setEnd_price(String end_price) {
        this.end_price = end_price;
    }

    public String getCoupon_price() {
        return coupon_price;
    }

    public void setCoupon_price(String coupon_price) {
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

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

}
