package com.lwf.projectpractice.flash_sale.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.lwf.projectpractice.flash_sale.domain.MiaoshaUser;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id = #{id}")
	public MiaoshaUser getById(@Param("id") long id);
}
