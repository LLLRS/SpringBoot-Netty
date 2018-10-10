package me.netty.service;

import me.netty.nettyServer.ChatMsg;
import me.netty.pojo.Users;
import me.netty.pojo.vo.FriendRequestVO;
import me.netty.pojo.vo.MyFriendsVO;

import java.util.List;

public  interface UserService {

    /**
     *  判断用户名是否存在
     */
    public boolean queryUsernameIsExist(String username);

    /**
     *  查询用户是否存在
     */
    public Users queryUserForLogin(String username, String pwd);

    /**
     * 用户注册
     */
    public Users saveUser(Users user);

    //TODO: 修改用户记录 - 通过好友请求

    /**
     * 查询好友列表
     */
    public List<MyFriendsVO> queryMyFriends(String userId);

    /**
     * 修改用户记录
     */
    public Users updateUserInfo(Users user);

    /**
     * 搜索朋友的前置条件
     */
    public Integer preconditionSearchFriends(String myUserId, String friendUsername);

    /**
     * 根据用户名查询用户对象
     */
    public Users queryUserInfoByUsername(String username);

    /**
     * 添加好友请求记录，保存到数据库
     */
    public void sendFriendRequest(String myUserId, String friendUsername);

    /**
     * 查询添加好友请求
     */
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    /**
     * 通过好友请求
     * 				1. 保存好友
     * 				2. 逆向保存好友
     * 				3. 删除好友请求记录
     */
    public void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 删除好友请求记录
     */
    public void deleteFriendRequest(String sendUserId, String acceptUserId);


    /**
     * 保存聊天消息到数据库
     */
    public String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收消息
     */
    public void updateMsgSigned(List<String> msgIdList);

    /**
     * 获取未签收消息列表
     */
    public List<me.netty.pojo.ChatMsg> getUnReadMsgList(String acceptUserId);
}
