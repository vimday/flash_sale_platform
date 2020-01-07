# flash_sale_platform
电商秒杀平台

## 阿里云相关配置
一般采用最新的稳定版本

centos 7.7

mysql 8.0.18

redis 5.0.7

配置时可能遇到的错误及解决方法：

CentOS7安装MySQL8.0：https://www.jianshu.com/p/a1765384f5ca

MySql8.0修改root密码：https://blog.csdn.net/wolf131721/article/details/93004013

注意要开启mysql跟redis的远程访问，修改相应配置文件，并开放相应端口


## 数据库表
### 创建秒杀用户
```mysql
CREATE TABLE miaosha_user(
id BIGINT(20) NOT NULL,
nickname VARCHAR(255) NOT NULL,
password VARCHAR(32) DEFAULT NULL COMMENT 'MD5(MD5(pass明文+固定salt)+salt)',
salt VARCHAR(10) DEFAULT NULL,
head VARCHAR(128) DEFAULT NULL COMMENT '头像云存储的ID',
register_date datetime DEFAULT NULL COMMENT '注册时间',
last_login_date datetime DEFAULT NULL COMMENT '上次登陆时间',
login_count int(11) DEFAULT NULL COMMENT '登录次数',
PRIMARY KEY(id)
)ENGINE=INNODB DEFAULT CHARSET=utf8mb4
```

