package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class BannerBean implements Serializable {

    public int id;
    public String pic;
    public String url;
    public String view_url;
    public String click_class;
    public String click_param;
    public String label;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getView_url() {
        return view_url;
    }

    public void setView_url(String view_url) {
        this.view_url = view_url;
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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
