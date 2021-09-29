package com.netty.websocket.channel;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 建立消息通道
 */
@Slf4j
public class UserChannelMap {

    /**
     * 定义一个channel组，管理所有的channel
     * GlobalEventExecutor.INSTANCE 是全局的事件执行器，是一个单例
     */
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存放用户与Chanel的对应信息，用于给指定用户发送消息
     */
    private static ConcurrentHashMap<String, Channel> userChannelMap = new ConcurrentHashMap<>();

    private UserChannelMap() {
    }

    /**
     * 添加用户id与channel的关联
     *
     * @param userNum
     * @param channel
     */
    public static void put(String userNum, Channel channel) {
        userChannelMap.put(userNum, channel);
    }

    /**
     * 根据用户id移除用户id与channel的关联
     *
     * @param userNum
     */
    public static void remove(String userNum) {
        userChannelMap.remove(userNum);
    }

    /**
     * 根据通道id移除用户与channel的关联
     *
     * @param channelId 通道的id
     */
    public static void removeByChannelId(String channelId) {
        if (!StringUtils.isNotBlank(channelId)) {
            return;
        }
        for (String s : userChannelMap.keySet()) {
            Channel channel = userChannelMap.get(s);
            if (channelId.equals(channel.id().asLongText())) {
                log.info("-->客户端连接断开,取消用户" + s + "与通道" + channelId + "的关联");
                userChannelMap.remove(s);
                break;
            }
        }
    }

    /**
     * 打印所有的用户与通道的关联数据
     */
    public static void print() {
        for (String s : userChannelMap.keySet()) {
            log.info("用户ID:【" + s + "】 通道:" + userChannelMap.get(s).id());
        }
    }

    /**
     * 根据好友id获取对应的通道
     *
     * @param receiverNum 接收人编号
     * @return Netty通道
     */
    public static Channel get(String receiverNum) {
        return userChannelMap.get(receiverNum);
    }

    /**
     * 获取channel组
     *
     * @return
     */
    public static ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    /**
     * 获取用户channel map
     *
     * @return
     */
    public static ConcurrentHashMap<String, Channel> getUserChannelMap() {
        return userChannelMap;
    }
}