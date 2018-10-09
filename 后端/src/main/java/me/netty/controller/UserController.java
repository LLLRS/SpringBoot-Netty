package me.netty.controller;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import me.netty.enums.MsgActionEnum;
import me.netty.enums.OperatorFriendRequestTypeEnum;
import me.netty.enums.SearchFriendsStatusEnum;
import me.netty.nettyServer.DataContent;
import me.netty.nettyServer.UserChannelRel;
import me.netty.pojo.Users;
import me.netty.pojo.bo.UsersBO;
import me.netty.pojo.vo.MyFriendsVO;
import me.netty.pojo.vo.UsersVO;
import me.netty.service.UserService;
import me.netty.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@RestController
@RequestMapping("u")

@Slf4j

public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FastDFSClient fastDFSClient;

    //用户登录
    @RequestMapping("login")
    public JSONResult login(@RequestBody Users user) throws Exception {

        //判断用户名和密码不能为空
        if(StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return JSONResult.errorMsg("用户名和密码不能为空...");
        }

        //判断用户名是否存在，存在 登录
        Users userResult = null;
        boolean isExist = userService.queryUsernameIsExist(user.getUsername());
        if(isExist){

            userResult = userService.queryUserForLogin(user.getUsername(),
                                MD5Utils.getMD5Str(user.getPassword()));
            if(userResult == null)
                return JSONResult.errorMsg("用户名或者密码不正确...");

        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);

        log.info("用户"+ user.getUsername() +"登录");
        return JSONResult.ok(usersVO);
    }


    //用户注册
    @RequestMapping("regist")
    public JSONResult regist(@RequestBody Users user) throws Exception {

        //判断用户名和密码不能为空
        if(StringUtils.isBlank(user.getUsername()) || StringUtils.isBlank(user.getPassword())){
            return JSONResult.errorMsg("用户名和密码不能为空...");
        }

        //判断用户名是否存在
        Users userResult = null;
        boolean isExist = userService.queryUsernameIsExist(user.getUsername());
        if(isExist){

            return JSONResult.errorMsg("用户名已存在...");
        }else{

            user.setNickname(user.getUsername());
            user.setFaceImage("M00/00/00/Q82PbFu3WC-AVGdQAAAJ85I4a2E924_80x80.png"); //默认图像
            user.setFaceImageBig("M00/00/00/Q82PbFu3WC-AVGdQAAAJ85I4a2E924.png");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.saveUser(user);
        }

        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);

        log.info("用户 "+ user.getUsername() +" 注册成功并登录");
        return JSONResult.ok(usersVO);
    }


    /**
     *  查询我的好友列表
     */
    @PostMapping("/myFriends")
    public JSONResult myFriends(String userId) {
        log.info("用户"+ userId +"查询好友列表");

        // 0. userId 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(userId);

        return JSONResult.ok(myFirends);
    }

    /**
     *  上传用户头像
     */
    @PostMapping("/uploadFaceBase64")
    public JSONResult uploadFaceBase64(@RequestBody UsersBO userBO) throws Exception {

        // 获取前端传过来的base64字符串, 然后转换为文件对象再上传
        String base64Data = userBO.getFaceData();
        String userFacePath = "F:\\test\\" + userBO.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath, base64Data);

        System.out.println("ok");
        // 上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);

        // 获取缩略图的url
        String thump = "_80x80.";
        String arr[] = url.split("\\.");
        String thumpImgUrl = arr[0] + thump + arr[1];

        System.out.println(thumpImgUrl);
        System.out.println(userBO.getUserId());

        // 更细用户头像
        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        Users result = userService.updateUserInfo(user);

        return JSONResult.ok(result);
    }

    /**
     *  设置用户昵称
     */
    @PostMapping("/setNickname")
    public JSONResult setNickname(@RequestBody UsersBO userBO) throws Exception {

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return JSONResult.ok(result);
    }

    /**
     *  搜索好友接口, 根据账号做匹配查询而不是模糊查询
     */
    @PostMapping("/search")
    public JSONResult searchUser(String myUserId, String friendUsername)
            throws Exception {

        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVO userVO = new UsersVO();
            BeanUtils.copyProperties(user, userVO);
            return JSONResult.ok(userVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errorMsg);
        }
    }

    /**
     *  发送添加好友的请求
     */
    @PostMapping("/addFriendRequest")
    public JSONResult addFriendRequest(String myUserId, String friendUsername)
            throws Exception {

        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return JSONResult.errorMsg("");
        }

        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status == SearchFriendsStatusEnum.SUCCESS.status) {
            userService.sendFriendRequest(myUserId, friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return JSONResult.errorMsg(errorMsg);
        }

        return JSONResult.ok();
    }

    /**
     *  发送添加好友的请求
     */
    @PostMapping("/queryFriendRequests")
    public JSONResult queryFriendRequests(String userId) {

        // 0. 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }

        // 1. 查询用户接受到的朋友申请
        return JSONResult.ok(userService.queryFriendRequestList(userId));
    }


    /**
     *  接受方 通过或者忽略朋友请求
     */
    @PostMapping("/operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId, String sendUserId,
                                             Integer operType) {
        System.out.println("operFriendRequest");
        // 0. acceptUserId sendUserId operType 判断不能为空
        if (StringUtils.isBlank(acceptUserId)
                || StringUtils.isBlank(sendUserId)
                || operType == null) {
            return JSONResult.errorMsg("");
        }

        // 1. 如果operType 没有对应的枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return JSONResult.errorMsg("");
        }

        if (operType == OperatorFriendRequestTypeEnum.IGNORE.type) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType == OperatorFriendRequestTypeEnum.PASS.type) {
            // 3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            //	   然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }

        // 4. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(acceptUserId);

        return JSONResult.ok(myFirends);
    }

    /**
     *
     *  用户手机端获取未签收的消息列表
     */
    @PostMapping("/getUnReadMsgList")
    public JSONResult getUnReadMsgList(String acceptUserId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(acceptUserId)) {
            return JSONResult.errorMsg("");
        }

        // 查询列表
        List<me.netty.pojo.ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);

        return JSONResult.ok(unreadMsgList);
    }
}
