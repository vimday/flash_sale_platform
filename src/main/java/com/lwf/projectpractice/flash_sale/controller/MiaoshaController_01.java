package com.lwf.projectpractice.flash_sale.controller;

import com.lwf.projectpractice.flash_sale.domain.MiaoshaOrder;
import com.lwf.projectpractice.flash_sale.domain.MiaoshaUser;
import com.lwf.projectpractice.flash_sale.rabbitmq.MQSender;
import com.lwf.projectpractice.flash_sale.rabbitmq.MiaoshaMessage;
import com.lwf.projectpractice.flash_sale.redis.GoodsKey;
import com.lwf.projectpractice.flash_sale.redis.MiaoshaKey;
import com.lwf.projectpractice.flash_sale.redis.OrderKey;
import com.lwf.projectpractice.flash_sale.redis.RedisService;
import com.lwf.projectpractice.flash_sale.result.CodeMsg;
import com.lwf.projectpractice.flash_sale.result.Result;
import com.lwf.projectpractice.flash_sale.service.GoodsService;
import com.lwf.projectpractice.flash_sale.service.MiaoshaService;
import com.lwf.projectpractice.flash_sale.service.MiaoshaUserService;
import com.lwf.projectpractice.flash_sale.service.OrderService;
import com.lwf.projectpractice.flash_sale.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;


@Controller
@RequestMapping("/miaoshaold")
public class MiaoshaController_01 implements InitializingBean {

    @Autowired
    MiaoshaUserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender sender;

	/*
	1306  5000*10
	 */
//原始版
//    @RequestMapping("/do_miaosha")
//    public String list(Model model, MiaoshaUser user,
//                       @RequestParam("goodsId") long goodsId) {
//        model.addAttribute("user", user);
//        if (user == null) {
//            return "login";
//        }
//        //判断库存
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goods.getStockCount();
//        if (stock <= 0) {
//            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
//            return "miaosha_fail";
//        }
//        //判断是否已经秒杀到了
//        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//        if (order != null) {
//            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
//            return "miaosha_fail";
//        }
//        //减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        model.addAttribute("orderInfo", orderInfo);
//        model.addAttribute("goods", goods);
//        return "order_detail";
//    }


    //修正逻辑并加缓存后后再zero下qps为4000+
    /**
     *  GET POST有什么区别？
     *  get是幂等的(不对服务端数据产生影响)
     *  post 修改服务端数据
     * */
//    @RequestMapping(value="/do_miaosha", method= RequestMethod.POST)
//    @ResponseBody
//    public Result<OrderInfo> miaosha(Model model, MiaoshaUser user,
//                                     @RequestParam("goodsId")long goodsId) {
//        model.addAttribute("user", user);
//        if(user == null) {
//            return Result.error(CodeMsg.SESSION_ERROR);
//        }
//        //判断库存
//        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
//        int stock = goods.getStockCount();
//        if(stock <= 0) {
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
//        //数据库层面的唯一联合索引保证不会多次秒杀到
//        //判断是否已经秒杀到了
//        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
//        if(order != null) {
//            return Result.error(CodeMsg.REPEATE_MIAOSHA);
//        }
//        //数据库层面保证不会超卖(数据库保证了并发的安全问题)
//        //减库存 下订单 写入秒杀订单
//        OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
//        return Result.success(orderInfo);
//    }

    /**
     * rabbitma队列+redis缓存+（异步下单）+前后端分离+内存标记
     * 在zero下qps为7000第二次为10000
     * 阿里云为1300第二次为2000
     */

    private HashMap<Long, Boolean> localOverMap = new HashMap<>();

    /**
     * 系统初始化
     */
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    /**
     * QPS:1306
     * 5000 * 10
     * QPS: 2114
     */
    @RequestMapping(value = "/do_miaosha", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //内存标记，减少redis访问
        boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);//10
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0);//排队中
    	/*
    	//判断库存
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);//10个商品，req1 req2
    	int stock = goods.getStockCount();
    	if(stock <= 0) {
    		return Result.error(CodeMsg.MIAO_SHA_OVER);
    	}
    	//判断是否已经秒杀到了
    	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
    	if(order != null) {
    		return Result.error(CodeMsg.REPEATE_MIAOSHA);
    	}
    	//减库存 下订单 写入秒杀订单
    	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
        return Result.success(orderInfo);
        */
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     */
    @RequestMapping(value = "/result", method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }

}
