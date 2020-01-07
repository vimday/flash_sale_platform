package com.lwf.projectpractice.flash_sale.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

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


@Service
public class MiaoshaUserService {
	
	
	public static final String COOKI_NAME_TOKEN = "token";

	private final MiaoshaUserDao miaoshaUserDao;
	private final RedisService redisService;

	@Autowired
	public MiaoshaUserService(MiaoshaUserDao miaoshaUserDao,RedisService redisService){
		this.miaoshaUserDao=miaoshaUserDao;
		this.redisService=redisService;
	}

	public MiaoshaUser getById(long id) {
		return miaoshaUserDao.getById(id);
	}
	
	public MiaoshaUser getByToken(String token){
		if(StringUtils.isEmpty(token))
			return null;
		return redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
	}

	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)) {
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		//延长有效期
		if(user != null) {
			addCookie(response, token, user);
		}
		return user;
	}
	

	public boolean login(HttpServletResponse response, LoginVo loginVo) {
		if(loginVo == null) {
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//判断手机号是否存在
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null) {
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//验证密码
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
		if(!calcPass.equals(dbPass)) {
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		//生成cookie
		String token	 = UUIDUtil.uuid();
		addCookie(response, token, user);
		return true;
	}
	
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user) {
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}

}
