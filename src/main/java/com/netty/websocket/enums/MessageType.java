package com.netty.websocket.enums;

public enum MessageType {
    /**
     * 心跳包
     */
    KEEPALIVE(0, "KEEPALIVE"),
    /**
     * 数据包
     */
    DATA(1, "DATA");

    private int value;
    private String type;

    MessageType(int value, String type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

