package com.lwf.projectpractice.flash_sale.redis;

public class GoodsKey extends BasePrefix {

    public static GoodsKey getGoodsList = new GoodsKey(60, "gl");
    public static GoodsKey getGoodsDetail = new GoodsKey(60, "gd");
    private GoodsKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
}