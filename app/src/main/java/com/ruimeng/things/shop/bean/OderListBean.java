package com.ruimeng.things.shop.bean;

import java.io.Serializable;

/**
 * Created by dww on 2017/9/22.
 */

public class OderListBean implements Serializable {

    private String id;
    private String order_id;
    private String pic;
    private String item_id;
    private String old_price;
    private String coupon_price;
    private String nts_balance;
    private String created;
    private String title;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrder_id() {
        return order_id;
    }

    public void setOrder_id(String order_id) {
        this.order_id = order_id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getItem_id() {
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getOld_price() {
        return old_price;
    }

    public void setOld_price(String old_price) {
        this.old_price = old_price;
    }

    public String getCoupon_price() {
        return coupon_price;
    }

    public void setCoupon_price(String coupon_price) {
        this.coupon_price = coupon_price;
    }

    public String getNts_balance() {
        return nts_balance;
    }

    public void setNts_balance(String nts_balance) {
        this.nts_balance = nts_balance;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
