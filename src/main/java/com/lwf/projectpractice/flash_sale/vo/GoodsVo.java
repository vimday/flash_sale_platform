package com.lwf.projectpractice.flash_sale.vo;

import java.util.Date;

import com.lwf.projectpractice.flash_sale.domain.Goods;

public class GoodsVo extends Goods {
	private Double miaoshaPrice;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
	public Integer getStockCount() {
		return stockCount;
	}
	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Double getMiaoshaPrice() {
		return miaoshaPrice;
	}
	public void setMiaoshaPrice(Double miaoshaPrice) {
		this.miaoshaPrice = miaoshaPrice;
	}

	@Override
	public String toString() {
		return "GoodsVo{" +
				"miaoshaPrice=" + miaoshaPrice +
				", stockCount=" + stockCount +
				", startDate=" + startDate +
				", endDate=" + endDate +
				'}';
	}
}
