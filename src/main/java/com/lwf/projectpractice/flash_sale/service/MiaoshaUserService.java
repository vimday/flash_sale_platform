package com.lwf.projectpractice.flash_sale.service;

import com.lwf.projectpractice.flash_sale.dao.MiaoshaUserDao;
import com.lwf.projectpractice.flash_sale.domain.MiaoshaUser;
import com.lwf.projectpractice.flash_sale.exception.GlobalException;
import com.lwf.projectpractice.flash_sale.redis.MiaoshaUserKey;
import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.result.CodeMsg;
import com.lwf.projectpractice.flash_sale.util.MD5Util;
import com.lwf.projectpractice.flash_sale.util.UUIDUtil;
import com.lwf.projectpractice.flash_sale.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;


@Service
public class MiaoshaUserService {


    public static final String COOKI_NAME_TOKEN = "token";

    //一个service引用别人的时候 一定能要引用sesrvice级别的，因为service一般涉及缓存 ，而dao不涉及
    private MiaoshaUserDao miaoshaUserDao;
    private RedisService redisService;

    @Autowired
    public MiaoshaUserService(MiaoshaUserDao miaoshaUserDao, RedisService redisService) {
        this.miaoshaUserDao = miaoshaUserDao;
        this.redisService = redisService;
    }

    public MiaoshaUser getById(long id) {
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (user != null)
            return user;

        user = miaoshaUserDao.getById(id);
        if (user != null)
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        return user;
    }

    public boolean updatePassword(String token, long id, String passwordNew) {
        MiaoshaUser user = getById(id);
        if (user == null)
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        //更新数据库
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(passwordNew, user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById, "" + id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token, token, user);
        return true;
    }

    public MiaoshaUser getByToken(String token) {
        if (StringUtils.isEmpty(token))
            return null;
        return redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
    }

    public MiaoshaUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        //延长有效期
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }


    public String login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
        redisService.set(MiaoshaUserKey.token, token, user);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
