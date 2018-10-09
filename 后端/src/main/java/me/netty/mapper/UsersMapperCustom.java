package me.netty.mapper;

import me.netty.pojo.Users;
import me.netty.pojo.vo.FriendRequestVO;
import me.netty.pojo.vo.MyFriendsVO;
import me.netty.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {
	
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	public List<MyFriendsVO> queryMyFriends(String userId);
	
	public void batchUpdateMsgSigned(List<String> msgIdList);
	
}