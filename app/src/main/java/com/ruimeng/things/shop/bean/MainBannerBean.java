package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class MainBannerBean implements Serializable {

    public String label;
    public String img;
    public String click_class;
    public String click_param;
    public String click_url;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getClick_class() {
        return click_class;
    }

    public void setClick_class(String click_class) {
        this.click_class = click_class;
    }

    public String getClick_param() {
        return click_param;
    }

    public void setClick_param(String click_param) {
        this.click_param = click_param;
    }

    public String getClick_url() {
        return click_url;
    }

    public void setClick_url(String click_url) {
        this.click_url = click_url;
    }
}
