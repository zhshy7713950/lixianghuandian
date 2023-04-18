package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class DynamicMsgBean implements Serializable {

    public String created;
    public String username;

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
