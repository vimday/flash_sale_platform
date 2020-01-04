package com.lwf.projectpractice.flash_sale.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.lwf.projectpractice.flash_sale.domain.User;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserDao {
	
	@Select("select * from user where id = #{id}")
	public User getById(@Param("id") int id);

	@Insert("insert into user(id, name)values(#{id}, #{name})")
	public int insert(User user);
	
}
