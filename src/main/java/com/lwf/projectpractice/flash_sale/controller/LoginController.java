package com.lwf.projectpractice.flash_sale.controller;

import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.result.Result;
import com.lwf.projectpractice.flash_sale.service.MiaoshaUserService;
import com.lwf.projectpractice.flash_sale.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);


    private MiaoshaUserService userService;
    private RedisService redisService;

    @Autowired
    public LoginController(MiaoshaUserService miaoshaUserService, RedisService redisService) {
        this.userService = miaoshaUserService;
        this.redisService = redisService;
    }

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        //登录
        String token = userService.login(response, loginVo);
        return Result.success(token);
    }
}
