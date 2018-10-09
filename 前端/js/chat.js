// 构建聊天业务CHAT
window.CHAT = {
	socket: null,
	init: function() {
		if (window.WebSocket) {
			
			// 如果当前的状态已经连接，那就不需要重复初始化websocket
			if (CHAT.socket != null 
				&& CHAT.socket != undefined 
				&& CHAT.socket.readyState == WebSocket.OPEN) {
				return false;
			}
			
			CHAT.socket = new WebSocket(app.nettyServerUrl);
			CHAT.socket.onopen = CHAT.wsopen,
			CHAT.socket.onclose = CHAT.wsclose,
			CHAT.socket.onerror = CHAT.wserror,
			CHAT.socket.onmessage = CHAT.wsmessage;
		}
		
	},
	
	chat: function(msg) {
		
		// 如果当前websocket的状态是已打开，则直接发送， 否则重连
		if (CHAT.socket != null 
				&& CHAT.socket != undefined  
				&& CHAT.socket.readyState == WebSocket.OPEN) {
				CHAT.socket.send(msg);
		} else {
			// 重连websocket
			CHAT.init();
			setTimeout("CHAT.reChat('" + msg + "')", "1000");
		}
		// 渲染快照列表进行展示
		CHAT.loadingChatSnapshot();
	},
	
	
	reChat: function(msg) {
//		console.log("消息重新发送...");
		CHAT.socket.send(msg);
	},
	
	wsopen: function() {
		console.log("websocket连接已建立...");
		
		var me = app.getUserGlobalInfo();
		// 构建ChatMsg
		var chatMsg = new app.ChatMsg(me.id, null, null, null);
		// 构建DataContent
		var dataContent = new app.DataContent(app.CONNECT, chatMsg, null);
		// 发送websocket
		CHAT.chat(JSON.stringify(dataContent));
		
		// 每次连接之后，获取用户的未读未签收消息列表
		CHAT.fetchUnReadMsg();
		
		//每18秒发送心跳
		setInterval("CHAT.keepalive()", 18000);
	},
	
	
	wsmessage: function(e) {
//		console.log("接受到消息：" + e.data);
		
		// 转换DataContent为对象
		var dataContent = JSON.parse(e.data);
		var action = dataContent.action;
		//重新拉取好友列表
		if (action === app.PULL_FRIEND) {
			contact.fetchContactList();
			return false;						
		}
		
		// 已登录
		if (action === app.ISLOGIN) {
//			console.log("用户在其他地方登录");
//			contact.rederAlertPage();
			$('#chat-modal-7').modal();
			
//			app.userLogout();
			return false;						
		}
		
		
		// 获取聊天消息模型，渲染接收到的聊天记录
		var chatMsg = dataContent.chatMsg;
		var msg = chatMsg.msg;
		var friendUserId = chatMsg.senderId;
		var myId = chatMsg.receiverId;
		
//		console.log(app.getContactList());
		var chat = document.getElementById("chatbox_" + friendUserId);
		var fd = app.getFriendFromContactList(friendUserId);
		chat.innerHTML += '<li class="other"><img src="'+app.imgServerUrl + fd.friendFaceImage+'"><span>'+ msg +'</span></li>';
		var scroll_div = document.getElementById("scroll_div_" + friendUserId);
		scroll_div.scrollTop = scroll_div.scrollHeight;


		var isRead = true;	// 设置消息的默认状态为已读
		var id = document.getElementById("talk_hiden").value;
		if(app.isNotNull(id)&&id != friendUserId){
			//聊天页面没有打开或者不是当前用户，标记消息未读状态
			isRead = false; 
		}
		

		// 接受到消息之后，对消息记录进行签收
		var dataContentSign = new app.DataContent(app.SIGNED, null, chatMsg.msgId);
		CHAT.chat(JSON.stringify(dataContentSign));
		
		// 保存聊天历史记录到本地缓存
		app.saveUserChatHistory(myId, friendUserId, msg, 2);
		app.saveUserChatSnapshot(myId, friendUserId, msg, isRead);
		
		// 渲染快照列表进行展示
		CHAT.loadingChatSnapshot();
	},
	
	
	wsclose: function() {
		console.log("连接关闭...");
	},
	wserror: function() {
		console.log("发生错误...");
	},
	
	// 展示聊天快照，渲染列表
	loadingChatSnapshot : function() {
		var user = app.getUserGlobalInfo();
		var chatSnapshotList = app.getUserChatSnapshot(user.id);
		
		var chatItemHtml = "";
		var ul_chatSnapshot = document.getElementById("friendlist");
		ul_chatSnapshot.innerHTML = "";
		for (var k = 0 ; k < chatSnapshotList.length ; k ++) {
			var chatItem = chatSnapshotList[k];

			var friendId = chatItem.friendId;

			// 根据friendId从本地联系人列表的缓存中拿到相关信息
			var friend = app.getFriendFromContactList(friendId);
//			console.log(friend);

			// 判断消息的已读或未读状态
			var isRead = chatItem.isRead;
			var readHtmlBefore = '';
			var readHtmlAfter = '';
			if (!isRead) {
				readHtmlBefore = '<span class="red-point">';
				readHtmlAfter = '</span>';
			}
			
			chatItemHtml =  ' <li class="user_active" friendUserId= "' + friend.friendUserId + '" > ' +
								' <div class="user_head"><img src="' + app.imgServerUrl + friend.friendFaceImage  + '"/></div> ' +
								' <div class="user_text"> ' +
									' <p class="user_name"> ' + readHtmlBefore + friend.friendNickname + readHtmlAfter + ' </p> ' +
									' <p class="user_message"> ' +chatItem.msg +'</p> ' +
								' </div> ' +
							' </li> ';
							
			ul_chatSnapshot.insertAdjacentHTML('beforeend', chatItemHtml);
		}
		
	},
	
	signMsgList: function(unSignedMsgIds) {
		// 构建批量签收对象的模型
		var dataContentSign = new app.DataContent(app.SIGNED,null,unSignedMsgIds);
		
		// 发送批量签收的请求
		CHAT.chat(JSON.stringify(dataContentSign));
	},
	

	// 每次重连后获取服务器的未签收消息
	fetchUnReadMsg: function() {
		var user = app.getUserGlobalInfo();
		var msgIds = ",";	// 格式：  ,1001,1002,1003,
		$.ajax({
			url: app.serverUrl + "/u/getUnReadMsgList?acceptUserId=" + user.id,
			data:{},
			dataType:'json',//服务器返回json格式数据
			type:'post',//HTTP请求类型
			timeout:10000,//超时时间设置为10秒；
			headers:{'Content-Type':'application/json'},	              
			success:function(data){
				
				if (data.status == 200) {
					var unReadMsgList = data.data;
//					console.log(JSON.stringify(unReadMsgList));
					
					for (var i = 0 ; i < unReadMsgList.length ; i ++) {
						var me = unReadMsgList[i];
						// 逐条存入聊天记录
						app.saveUserChatHistory(me.acceptUserId, me.sendUserId, me.msg, 2);
						// 存入聊天快照
						app.saveUserChatSnapshot(me.acceptUserId, me.sendUserId, me.msg, false);
						// 拼接批量接受的消息id字符串，逗号间隔
						msgIds += me.id;
						msgIds += ",";
					}
					
					// 调用批量签收的方法
					CHAT.signMsgList(msgIds);
					// 刷新快照
					
				}
			}
		});
		
		CHAT.loadingChatSnapshot();
	},
		
	keepalive: function() {
		// 构建对象
		var dataContent = new app.DataContent(app.KEEPALIVE, null, null);
		// 发送心跳
		CHAT.chat(JSON.stringify(dataContent));
		
		// 定时执行
		CHAT.fetchUnReadMsg();
		contact.fetchContactList();
		contact.fetchFriendRequests();
	}
};