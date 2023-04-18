package com.ruimeng.things.shop.bean;

import java.io.Serializable;
import java.util.List;


public class GetMainBean implements Serializable {

    public List<BannerBean> banner;
    public String led;
    public List<TopBtnBean> top_btn;
    public List<MainBannerBean> main_banner;
    public List<DiyBtnGroupBean> diy_btn_group;
    public List<ItemsBean> items;
    public List<ItemsBean> recommend_items;

    public List<BannerBean> getBanner() {
        return banner;
    }

    public void setBanner(List<BannerBean> banner) {
        this.banner = banner;
    }

    public String getLed() {
        return led;
    }

    public void setLed(String led) {
        this.led = led;
    }

    public List<TopBtnBean> getTop_btn() {
        return top_btn;
    }

    public void setTop_btn(List<TopBtnBean> top_btn) {
        this.top_btn = top_btn;
    }

    public List<MainBannerBean> getMain_banner() {
        return main_banner;
    }

    public void setMain_banner(List<MainBannerBean> main_banner) {
        this.main_banner = main_banner;
    }

    public List<DiyBtnGroupBean> getDiy_btn_group() {
        return diy_btn_group;
    }

    public void setDiy_btn_group(List<DiyBtnGroupBean> diy_btn_group) {
        this.diy_btn_group = diy_btn_group;
    }

    public List<ItemsBean> getItems() {
        return items;
    }

    public void setItems(List<ItemsBean> items) {
        this.items = items;
    }

    public List<ItemsBean> getRecommend_items() {
        return recommend_items;
    }

    public void setRecommend_items(List<ItemsBean> recommend_items) {
        this.recommend_items = recommend_items;
    }
}
