package com.ruimeng.things.shop.bean;

import java.io.Serializable;
import java.util.List;


public class GetShareBean implements Serializable {

    private String text;
    private String share_png;
    private String url;
    private List<String> pics;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getShare_png() {
        return share_png;
    }

    public void setShare_png(String share_png) {
        this.share_png = share_png;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

}
