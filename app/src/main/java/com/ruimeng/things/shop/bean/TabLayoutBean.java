package com.ruimeng.things.shop.bean;

import java.io.Serializable;


public class TabLayoutBean implements Serializable {

    public String id;
    public String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
