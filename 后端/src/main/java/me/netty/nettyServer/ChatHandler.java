package me.netty.nettyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import me.netty.SpringUtil;
import me.netty.enums.MsgActionEnum;
import me.netty.utils.JsonUtils;
import me.netty.service.UserService;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
/**
 *
 * @Description: 处理消息的handler
 * TextWebSocketFrame： 在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 */

public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channle
    public static ChannelGroup  users = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg)
            throws Exception {
        String content = msg.text();
        Channel currentChannel = ctx.channel();

        // 获取客户端发来的消息
        DataContent dataContent = JsonUtils.jsonToPojo(content, DataContent.class);
        Integer action = dataContent.getAction();

        // 判断消息类型，并做相应的处理
        if (action == MsgActionEnum.CONNECT.type) {
            String senderId = dataContent.getChatMsg().getSenderId();

            //判断用户是否存在其他地方登录
            Channel sendChannel = UserChannelRel.get(senderId);
            if (sendChannel != null) {
                // 使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
                DataContent data = new DataContent();
                data.setAction(MsgActionEnum.ISLOGIN.type);

                sendChannel.writeAndFlush(
                        new TextWebSocketFrame(
                                JsonUtils.objectToJson(data)));
            }



            //把最新的channel和userid关联起来
            UserChannelRel.put(senderId, currentChannel);

            System.out.println("tttt:  "+senderId + currentChannel );
            log.info(senderId + currentChannel);
            // 测试
//            for (Channel c : users) {
//                System.out.println(c.id().asLongText());
//            }
//            UserChannelRel.output();
        } else if (action == MsgActionEnum.CHAT.type) {
            // 聊天类型的消息，把聊天记录保存到数据库，同时标记消息的签收状态[未签收]

            ChatMsg chatMsg = dataContent.getChatMsg();
            String msgText = chatMsg.getMsg();
            String receiverId = chatMsg.getReceiverId();
            String senderId = chatMsg.getSenderId();

            // 保存消息到数据库，并且标记为 未签收
            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
            String msgId = userService.saveMsg(chatMsg);
            chatMsg.setMsgId(msgId);

            DataContent dataContentMsg = new DataContent();
            dataContentMsg.setChatMsg(chatMsg);

            // 发送消息
            // 从全局用户Channel关系中获取接受方的channel
            Channel receiverChannel = UserChannelRel.get(receiverId);
            if (receiverChannel != null) {
                // 当receiverChannel不为空的时候，从ChannelGroup去查找对应的channel是否存在
                Channel findChannel = users.find(receiverChannel.id());
                if (findChannel != null) {
                    // 用户在线
                    receiverChannel.writeAndFlush(
                            new TextWebSocketFrame(
                                    JsonUtils.objectToJson(dataContentMsg)));
                }
            }

        } else if (action == MsgActionEnum.SIGNED.type) {
            // 签收消息类型，针对具体的消息进行签收，修改数据库中对应消息的签收状态[已签收]

            UserService userService = (UserService)SpringUtil.getBean("userServiceImpl");
            // 扩展字段在signed类型的消息中，代表需要去签收的消息id，逗号间隔
            String msgIdsStr = dataContent.getExtand();
            String msgIds[] = msgIdsStr.split(",");

            List<String> msgIdList = new ArrayList<>();
            for (String mid : msgIds) {
                if (StringUtils.isNotBlank(mid)) {
                    msgIdList.add(mid);
                }
            }

            System.out.println(msgIdList.toString());

            if (msgIdList != null && !msgIdList.isEmpty() && msgIdList.size() > 0) {
                // 批量签收
                userService.updateMsgSigned(msgIdList);
            }

        } else if (action == MsgActionEnum.KEEPALIVE.type) {
            // 心跳类型的消息
            log.info("收到来自channel为[" + currentChannel + "]的心跳包...");
        }

    }

    /**
     * 当客户端连接服务端之后（打开连接）
     * 获取客户端的channle，并且放到ChannelGroup中去进行管理
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

        String channelId = ctx.channel().id().asShortText();
        log.info("客户端被移除，channelId为：" + channelId);

        //当触发handlerRemoved channelGroup会自动移除对应客户端的channel
        users.remove(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        // 发生异常之后关闭连接（关闭channel），随后从ChannelGroup中移除
        ctx.channel().close();
        users.remove(ctx.channel());
    }


}
