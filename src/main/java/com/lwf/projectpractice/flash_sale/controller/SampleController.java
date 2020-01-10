package com.lwf.projectpractice.flash_sale.controller;

import com.lwf.projectpractice.flash_sale.domain.User;
import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.redis.UserKey;
import com.lwf.projectpractice.flash_sale.result.CodeMsg;
import com.lwf.projectpractice.flash_sale.result.Result;
import com.lwf.projectpractice.flash_sale.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {


    private final UserService userService;

    private final RedisService redisService;

    @Autowired
    public SampleController(UserService userService, RedisService redisService) {
        this.userService = userService;
        this.redisService = redisService;
    }


    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> home() {
        return Result.success("Hello，world");
    }

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SESSION_ERROR);
    }

    @RequestMapping("/hello/themaleaf")
    public String themaleaf(Model model) {
        model.addAttribute("name", "Joshua");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet() {
        User user = userService.getById(1);
        return Result.success(user);
    }


    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user = redisService.get(UserKey.getById, "" + 1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById, "" + 1, user);//UserKey:id1
        return Result.success(true);
    }


}
