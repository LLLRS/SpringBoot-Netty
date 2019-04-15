package me.netty.controller;

import me.netty.pojo.Users;
import me.netty.service.UserService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestExecutionListeners;

public class test {
    @Autowired
    UserService userService;

    @Test
    public void test1(){
        Users user = userService.queryUsernameIsExist("test1");
        System.out.println(user);
    }
}
