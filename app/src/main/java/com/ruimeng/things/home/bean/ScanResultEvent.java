package com.ruimeng.things.home.bean;

public class ScanResultEvent {
    private String deviceId;
    private int type;

    public ScanResultEvent(String deviceId, int type) {
        this.deviceId = deviceId;
        this.type = type;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
