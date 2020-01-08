package com.lwf.projectpractice.flash_sale.controller;

import java.util.List;

import com.lwf.projectpractice.flash_sale.domain.MiaoshaUser;
import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.service.GoodsService;
import com.lwf.projectpractice.flash_sale.service.MiaoshaUserService;
import com.lwf.projectpractice.flash_sale.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/goods")
public class GoodsController {


	private final MiaoshaUserService userService;
	private final RedisService redisService;
	private final GoodsService goodsService;

	@Autowired
	public GoodsController(MiaoshaUserService miaoshaUserService,RedisService redisService,GoodsService goodsService){
		this.userService=miaoshaUserService;
		this.redisService=redisService;
		this.goodsService=goodsService;
	}

//	@RequestMapping("test")
//	public String test(Model model){
//		model.addAttribute("user",new MiaoshaUser());
//		return "goods_list";
//	}

	//只需要写业务代码,业务逻辑放到了 userargumentresolver里面
	@RequestMapping("/test_webconfigure")
	public String testWebconfigureWithoutGoods(Model model,MiaoshaUser user){
		model.addAttribute("user",user);
		return "goods_list";
	}

	//第一次把cookietoken的注解也写成requestparam了(为了兼容手机端有可能把cookie写到参数里） 还单独写了上面的test函数
	//写错的话两个参数都为空 在pc端测试自然两个参数都为空，就跳转不到goods_list页了
	@RequestMapping("/testtoken")
	public String toLogin(Model model,HttpServletResponse response,
						  @CookieValue(value = MiaoshaUserService.COOKI_NAME_TOKEN,required = false) String cookieToken,
						  @RequestParam(value = MiaoshaUserService.COOKI_NAME_TOKEN,required = false) String paramToken){
		//System.out.println(".................................");
		if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken))
			return "login";
		//System.out.println("+++++++++++++++++++++++++++");
		String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		model.addAttribute("user",user);
		//System.out.println("--------------------------");
		//System.out.println(user);
		//System.out.println("===================");
		return "goods_list";
	}

    @RequestMapping("/to_list")
    public String list(Model model, MiaoshaUser user) {
		//System.out.println(user);
    	model.addAttribute("user", user);
    	//查询商品列表
    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
    	//压测，不登录 user为null 也可以获取商品列表
//    	for (GoodsVo goodsVo:goodsList)
//			System.out.println(goodsVo);
    	model.addAttribute("goodsList", goodsList);
        return "goods_list";
    }
    
    @RequestMapping("/to_detail/{goodsId}")
    public String detail(Model model,MiaoshaUser user,
    		@PathVariable("goodsId")long goodsId) {
    	model.addAttribute("user", user);
    	
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
    	model.addAttribute("goods", goods);
    	
    	long startAt = goods.getStartDate().getTime();
    	long endAt = goods.getEndDate().getTime();
    	long now = System.currentTimeMillis();
    	
    	int miaoshaStatus = 0;
    	int remainSeconds = 0;
    	if(now < startAt ) {//秒杀还没开始，倒计时
    		miaoshaStatus = 0;
    		remainSeconds = (int)((startAt - now )/1000);
    	}else  if(now > endAt){//秒杀已经结束
    		miaoshaStatus = 2;
    		remainSeconds = -1;
    	}else {//秒杀进行中
    		miaoshaStatus = 1;
    		remainSeconds = 0;
    	}
    	model.addAttribute("miaoshaStatus", miaoshaStatus);
    	model.addAttribute("remainSeconds", remainSeconds);
        return "goods_detail";
    }
    
}
