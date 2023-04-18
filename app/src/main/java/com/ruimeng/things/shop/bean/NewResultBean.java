package com.ruimeng.things.shop.bean;

import java.util.List;

/**
 * Created by dww on 2017/9/21.
 */
public class NewResultBean {
    /**
     * record_total : 62452
     * item : [{"item_id":"40208925350","title":"顺事宝三层卫浴转角浴室架","old_price":"49.90","end_price":"39.90","coupon_price":"10.00","coupon_begin":"2017-09-22 16:10:53","coupon_end":"2017-09-23 23:59:59","sales":"0","master_image":"https://img.alicdn.com/bao/uploaded/i2/TB1_AVvOpXXXXarXXXXXXXXXXXX_!!0-item_pic.jpg","created":"1506067853"},{"item_id":"554989778919","title":"斯柯达明锐昕锐全包冰丝坐垫","old_price":"268.00","end_price":"188.00","coupon_price":"80.00","coupon_begin":"2017-09-22 16:23:27","coupon_end":"2017-09-24 23:59:59","sales":"0","master_image":"https://gd3.alicdn.com/imgextra/i3/2998273333/TB2Obp2s0FopuFjSZFHXXbSlXXa_!!2998273333.jpg","created":"1506068607"}]
     */

    private String record_total;
    private List<ItemBean> item;

    public String getRecord_total() {
        return record_total;
    }

    public void setRecord_total(String record_total) {
        this.record_total = record_total;
    }

    public List<ItemBean> getItem() {
        return item;
    }

    public void setItem(List<ItemBean> item) {
        this.item = item;
    }

    public static class ItemBean {
        /**
         * item_id : 40208925350
         * title : 顺事宝三层卫浴转角浴室架
         * old_price : 49.90
         * end_price : 39.90
         * coupon_price : 10.00
         * coupon_begin : 2017-09-22 16:10:53
         * coupon_end : 2017-09-23 23:59:59
         * sales : 0
         * master_image : https://img.alicdn.com/bao/uploaded/i2/TB1_AVvOpXXXXarXXXXXXXXXXXX_!!0-item_pic.jpg
         * created : 1506067853
         */

        private String item_id;
        private String title;
        private String old_price;
        private String end_price;
        private String coupon_price;
        private String coupon_begin;
        private String coupon_end;
        private String sales;
        private String master_image;
        private String created;
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

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getItem_type() {
            return item_type;
        }

        public void setItem_type(String item_type) {
            this.item_type = item_type;
        }
    }
}
