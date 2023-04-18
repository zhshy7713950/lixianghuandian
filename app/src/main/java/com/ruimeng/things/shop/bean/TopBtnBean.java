package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class TopBtnBean implements Serializable {

    public String btn_label;
    public String btn_img;
    public String click_class;
    public String click_url;
    public String click_param;

    public String getBtn_label() {
        return btn_label;
    }

    public void setBtn_label(String btn_label) {
        this.btn_label = btn_label;
    }

    public String getBtn_img() {
        return btn_img;
    }

    public void setBtn_img(String btn_img) {
        this.btn_img = btn_img;
    }

    public String getClick_class() {
        return click_class;
    }

    public void setClick_class(String click_class) {
        this.click_class = click_class;
    }

    public String getClick_url() {
        return click_url;
    }

    public void setClick_url(String click_url) {
        this.click_url = click_url;
    }

    public String getClick_param() {
        return click_param;
    }

    public void setClick_param(String click_param) {
        this.click_param = click_param;
    }
}
