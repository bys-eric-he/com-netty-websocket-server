package com.netty.websocket.pojo;

import com.netty.websocket.enums.MessageType;

import java.io.Serializable;

/**
 * 数据包
 */
public class MessageDataContent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 扩展字段
     */
    private String extend;

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }
}
