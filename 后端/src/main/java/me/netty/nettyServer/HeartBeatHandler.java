package me.netty.nettyServer;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义的空闲状态检测handler
 */

@Slf4j
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
		// 判读是否读写空闲
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换
			if (event.state() == IdleState.ALL_IDLE) {
				
				log.info("channel关闭前，users的数量为：" + ChatHandler.users.size());

				// 关闭无用的channel
				Channel channel = ctx.channel();
				channel.close();

				log.info("channel关闭后，users的数量为：" + ChatHandler.users.size());
			}
		}
		
	}
	
}
