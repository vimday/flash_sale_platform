package com.lwf.projectpractice.flash_sale.controller;

import java.util.List;

import com.lwf.projectpractice.flash_sale.domain.MiaoshaUser;
import com.lwf.projectpractice.flash_sale.redis.GoodsKey;
import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.service.GoodsService;
import com.lwf.projectpractice.flash_sale.service.MiaoshaUserService;
import com.lwf.projectpractice.flash_sale.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/goods")
public class GoodsController {

	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	@Autowired
	ApplicationContext applicationContext;

	private  MiaoshaUserService userService;
	private  RedisService redisService;
	private  GoodsService goodsService;

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

	/*

	QPS:1267 5000*10
	 */

//    @RequestMapping("/to_list")
//    public String list(Model model, MiaoshaUser user) {
//		//System.out.println(user);
//    	model.addAttribute("user", user);
//    	//查询商品列表
//    	List<GoodsVo> goodsList = goodsService.listGoodsVo();
//    	//压测，不登录 user为null 也可以获取商品列表
////    	for (GoodsVo goodsVo:goodsList)
////			System.out.println(goodsVo);
//    	model.addAttribute("goodsList", goodsList);
//        return "goods_list";
//    }


	//redis页面缓存
	//qps 3000
	@RequestMapping(value="/to_list", produces="text/html")
	@ResponseBody
	public String list(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user) {
		model.addAttribute("user", user);
		//取缓存
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
//    	 return "goods_list";
		WebContext ctx = new WebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap());
		//手动渲染
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}

	@RequestMapping(value="/to_detail2/{goodsId}",produces="text/html")
	@ResponseBody
	public String detail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,
						  @PathVariable("goodsId")long goodsId) {
		model.addAttribute("user", user);

		//取缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)) {
			return html;
		}
		//手动渲染
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
//        return "goods_detail";

		WebContext ctx = new WebContext(request,response,
				request.getServletContext(),request.getLocale(), model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", ctx);
		if(!StringUtils.isEmpty(html)) {
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
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
