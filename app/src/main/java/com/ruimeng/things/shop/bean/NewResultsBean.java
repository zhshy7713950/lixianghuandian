package com.ruimeng.things.shop.bean;

import java.util.List;


public  class NewResultsBean
    {
        /**
         * record_total : 62452
         * item : [{"item_id":"40208925350","title":"顺事宝三层卫浴转角浴室架","old_price":"49.90","end_price":"39.90","coupon_price":"10.00","coupon_begin":"2017-09-22 16:10:53","coupon_end":"2017-09-23 23:59:59","sales":"0","master_image":"https://img.alicdn.com/bao/uploaded/i2/TB1_AVvOpXXXXarXXXXXXXXXXXX_!!0-item_pic.jpg","created":"1506067853"},{"item_id":"554989778919","title":"斯柯达明锐昕锐全包冰丝坐垫","old_price":"268.00","end_price":"188.00","coupon_price":"80.00","coupon_begin":"2017-09-22 16:23:27","coupon_end":"2017-09-24 23:59:59","sales":"0","master_image":"https://gd3.alicdn.com/imgextra/i3/2998273333/TB2Obp2s0FopuFjSZFHXXbSlXXa_!!2998273333.jpg","created":"1506068607"}]
         */

        private String record_total;
        private List<ItemsBean> item;

        public String getRecord_total ()
        {
            return record_total;
        }

        public void setRecord_total (String record_total)
        {
            this.record_total = record_total;
        }

        public List<ItemsBean> getItem ()
        {
            return item;
        }

        public void setItem (List<ItemsBean> item)
        {
            this.item = item;
        }

}
