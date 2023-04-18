package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class PopBean implements Serializable {

    public String id;
    public String pop_mode;
    public String content;
    public String clipboard;
    public String url;
    public String url_mode;
    public String pic;
    public String click_param;
    public String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPop_mode() {
        return pop_mode;
    }

    public void setPop_mode(String pop_mode) {
        this.pop_mode = pop_mode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getClipboard() {
        return clipboard;
    }

    public void setClipboard(String clipboard) {
        this.clipboard = clipboard;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl_mode() {
        return url_mode;
    }

    public void setUrl_mode(String url_mode) {
        this.url_mode = url_mode;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
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
