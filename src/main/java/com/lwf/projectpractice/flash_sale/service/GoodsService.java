package com.lwf.projectpractice.flash_sale.service;

import com.lwf.projectpractice.flash_sale.dao.GoodsDao;
import com.lwf.projectpractice.flash_sale.domain.MiaoshaGoods;
import com.lwf.projectpractice.flash_sale.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class GoodsService {


    private  GoodsDao goodsDao;

    @Autowired
    public GoodsService(GoodsDao goodsDao) {
        this.goodsDao = goodsDao;
    }

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    // 未优化秒杀前的减库存代码
//    public void reduceStock(GoodsVo goods) {
//        MiaoshaGoods g = new MiaoshaGoods();
//        g.setGoodsId(goods.getId());
//        goodsDao.reduceStock(g);
//    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        return ret > 0;
    }

    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            MiaoshaGoods g = new MiaoshaGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsDao.resetStock(g);
        }
    }
}
