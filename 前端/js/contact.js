window.contact = {
	
	// 获取后端所有好友列表
	fetchContactList : function() {
		 //todo 添加新的好友后应更新缓存
		
		var user = app.getUserGlobalInfo();
//		console.log(user.id);
		$.ajax({
			url: app.serverUrl + "/u/myFriends?userId=" + user.id,
			data:{},
			dataType:'json',//服务器返回json格式数据
			type:'post',//HTTP请求类型
			timeout:10000,//超时时间设置为10秒；
			headers:{'Content-Type':'application/json'},	              
			success:function(data){
				
				if (data.status == 200) {
					var contactList = data.data;

					app.setContactList(contactList);
//					console.log(app.getContactList());

				}
			}
		});
		
	},
		
		
	// 获取后端所有添加好友请求
	fetchFriendRequests : function() {
		
		var user = app.getUserGlobalInfo();
//		console.log(user.id);
		$.ajax({
			url:app.serverUrl + "/u/queryFriendRequests?userId=" + user.id,
			data:{},
			dataType:'json',//服务器返回json格式数据
			type:'post',//HTTP请求类型
			timeout:10000,//超时时间设置为10秒；
			headers:{'Content-Type':'application/json'},	              
			success:function(data){
				
				if (data.status == 200) {
					var contactList = data.data;

//					console.log(JSON.stringify(data.data));
					app.setFriendRequests(contactList);
//					console.log(app.getFriendRequests());

				}
			}
		});
		
	},	
	
	//构建所有联系人的聊天界面
	renderChatPage : function() {
		
		var friendList = app.getContactList();
		var contactHtml = '';
		for (var i = 0 ; i < friendList.length ; i ++) {
			//friendFaceImage friendNickname friendUserId friendUsername
			var friend = friendList[i];
			contactHtml += ' <li id="'+ friend.friendUserId +'" class="chatli_box" style="display : none"> ' +
								' <div class="windows_top"> ' +
									' <div class="windows_top_box"> ' +
										' <span> ' + friend.friendNickname  +' </span> ' +
										' <ul class="window_icon"> ' +
											' <li><a href=""><img src="image/icon/icon7.png"/></a></li> ' +
											' <li><a href=""><img src="image/icon/icon8.png"/></a></li> ' +
											' <li><a href=""><img src="image/icon/icon9.png"/></a></li> ' +
											' <li><a href=""><img src="image/icon/icon10.png"/></a></li> ' +
										' </ul> ' +
										
									' </div> ' +
								' </div> ' +
								
								' <!--聊天内容--> ' +
								' <div class="windows_body"> ' +
									' <div id="scroll_div_'+ friend.friendUserId +'"  style=" overflow:scroll;  height:480px;"> ' +
										' <ul class="content" id="chatbox_'+ friend.friendUserId +'"> ' +
							
										' </ul> ' +
									' </div> ' +
									
								' </div> ' +
								
								' <!--发送窗口--> ' +
								' <div class="windows_input"  id="talkbox_'+ friend.friendUserId +'" > ' +
										' <div class="input_icon"> ' +
											' <a href="javascript:;"></a> ' +
											' <a href="javascript:;"></a> ' +
											' <a href="javascript:;"></a> ' +
											' <a href="javascript:;"></a> ' +
											' <a href="javascript:;"></a> ' +
											' <a href="javascript:;"></a> ' +
										' </div> ' +
										' <div class="input_box"> ' +
											' <textarea  name="" rows="" cols="" id="input_box_'+ friend.friendUserId +'"> </textarea> ' +
											' <button class="send" onclick="doClick()" > 发送（S）</button> ' +
										' </div> ' +
							
								' </div> ' +
							' </li> ' ;
							
//							console.log("构建。。。。。。。。。。。。");
		}
		
		document.getElementById("talk_window_chat").innerHTML = contactHtml;
		
//		console.log("构建所有联系人的聊天界面");
	},
		
	
	// 从缓存中获取联系人和好友请求列表，并且渲染到页面
	renderContactPage : function() {
		// 构建通讯录二维数组模型
		var contactArray = [
			[],[],[],[],[],[],[],[],[],
			[],[],[],[],[],[],[],[],[],
			[],[],[],[],[],[],[],[],[]
		]
		
		// 26个字母外加 #号
		var enWords = [
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 
			'H', 'I', 'J', 'K', 'L', 'M', 'N',
			'O', 'P', 'Q', 'R', 'S', 'T', 
			'U', 'V', 'W', 'X', 'Y', 'Z',
			'#'
		]
		
	
		// 1. 获取联系人列表
		var friendList = app.getContactList();
		
	
		//新的好友列表
		var friendRequests = app.getFriendRequests();
		

		// 2. 循环好友列表，转换为拼音
		for (var i = 0 ; i < friendList.length ; i ++) {
			var friend = friendList[i];
			// 2.1 转换拼音
			var pingyin = words.convertPinyin(friend.friendNickname);
			// 2.2 截取拼音的首字母
			var firstChar = pingyin.substr(0, 1).toUpperCase();
			// 2.3 获取首字母在二维数组中的位置
			var order = enWords.length - 1;
			for (var j = 0 ; j < enWords.length ; j ++) {
				if (enWords[j] == firstChar) {
					order =  j;
					break;
				}
			}

			// 2.4 获得顺序之后，塞入朋友
			contactArray[order].push(friend);
		}

		// 3. 构建通讯录html进行渲染  新的好友 + 联系人
		var contactHtml = ' <li class="friends_box"> ' +
						  '		<p>添加好友请求</p> ' +
						  '	</li>';
						  
		//好友请求
		for (var k = 0 ; k < friendRequests.length ; k ++ ) {
			var friendRequest = friendRequests[k];
			
			//sendUserId  sendUsername  sendFaceImage   sendNickname
			contactHtml += '' +
				'<li friendUserId="'+ friendRequest.sendUserId +'" class="chat-with-friend friends_box">' +
					'<div class="user_head"><img src="'+  app.imgServerUrl+friendRequest.sendFaceImage +'"/></div>' +
					'<div class="friends_text">' +
						'<p class="user_name">' + friendRequest.sendNickname + '</p> ' +
					'</div>' +
				'</li>';
			
			
		}
		
		//好友列表 
		contactHtml  += ' <li class="friends_box"> ' +
						  '		<p>好友列表</p> ' +
						  '	</li>';
						  
					  
		for (var z = 0 ; z < contactArray.length ; z ++ ) {
			var friendArray = contactArray[z];
			if (friendArray.length > 0) {
				
				var nicknameStarter = enWords[z];
				contactHtml += '<p class="friends_text">' + nicknameStarter + '</p>';
				for (var x = 0 ; x < friendArray.length ; x ++ ) {
					//friendFaceImage friendNickname friendUserId friendUsername
					contactHtml += '' +
						'<li friendUserId="' + friendArray[x].friendUserId  +'" flag = "chatting" class="chat-with-friend friends_box">' +
							'<div class="user_head"><img src="'+  app.imgServerUrl+ friendArray[x].friendFaceImage  +'"/></div>' +
							'<div class="friends_text">' +
								'<p class="user_name">' + friendArray[x].friendNickname + '</p> ' +
							'</div>' +
						'</li>';
				}
			}
		}
		
		// 渲染html
		document.getElementById("contactList").innerHTML = contactHtml;
	
		// 清空数组
		contactArray = [
			[],[],[],[],[],[],[],[],[],
			[],[],[],[],[],[],[],[],[],
			[],[],[],[],[],[],[],[],[]
		]
		
		

	},
	

	// 重复登录警告框
	rederAlertPage : function(){
		
			var contactHtml = ' <div class="am-modal-dialog"> ' +
								    ' <div class="am-modal-hd"> 用户在其他地方登录'  + 
								      ' <a href="javascript: void(0)" class="am-close am-close-spin" data-am-modal-close> &times;</a> ' +			      
								    ' </div> ' +
								
									' <div class="modal-footer"> ' +
										' <input type="text" value="" id="sendUserId_comfirm" style="display:none"> </input> ' +
										' <button type="button" id="alert_submit" class="btn btn-primary" data-dismiss="modal" data-am-modal-close>确定</button> ' +			
									' </div> ' +
								    	    
							    ' </div> ';
							    
		    document.getElementById("chat-modal-7").innerHTML = contactHtml;
	}
		
}
