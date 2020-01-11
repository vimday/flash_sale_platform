# flash_sale_platform
电商秒杀平台
nginx做负载均衡，rabbitmq作消息队列，redis缓存，前后端分离，对象级与页面级粒度的缓存
满足高并发线程安全要求
用SpringBoot开发（也有传统的SSM版）
前端采用thymeleaf+静态缓存

同时在阿里云（centos）与局域网ubuntu主机 与本机（win10）上部署测试
采用jmeter做压测 

## TODO
完善前端页面

整合电商其他功能

把部分老架构更新成目前最新的稳定的替代品

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
### 创建商品表及插入商品
```mysql
CREATE TABLE goods(
id BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
goods_name VARCHAR(16) DEFAULT(NULL) COMMENT '商品名称',
goods_title VARCHAR(64) DEFAULT(NULL) COMMENT '商品标题',
goods_img VARCHAR(64) DEFAULT(NULL) COMMENT '商品图片',
goods_detail LONGTEXT COMMENT '商品详情',
goods_price int(11) DEFAULT('0.00') COMMENT '商品单价',
goods_stock int(11) DEFAULT('0') COMMENT '商品库存，-1表示无限',
PRIMARY KEY(id)
)ENGINE=INNODB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;
INSERT INTO goods
VALUES (1, 'iphonex', 'Apple iphone X (A1865) 64GB银色移动联通电信4G手机', '/img/iphonex.png',
        'Apple iphone X (A1865) 64GB银色移动联通电信4', 8765, 100);
INSERT INTO `goods`
VALUES (2, '华为Meta9', '华为 Mate 9 4GB+32GB版 月光银 移动联通电信4G手机 双卡双待', '/img/meta10.png',
        '华为 Mate 9 4GB+32GB版 月光银 移动联通电信4G手机 双卡双待', 3212.00, 10);
```
### 创建秒杀商品表及插入秒杀商品
```mysql
CREATE TABLE miaosha_goods
(
    id            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '秒杀的商品表',
    goods_id      bigint(20)     DEFAULT NULL COMMENT '商品ld',
    miaosha_price decimal(10, 2) DEFAULT 0.00 COMMENT '秒杀价',
    stock_count   int(11)        DEFAULT NULL COMMENT '库存数量',
    start_date    datetime       DEFAULT NULL COMMENT "秒杀开始时间",
    end_date      datetime       DEFAULT NULL COMMENT '秒杀结束时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀商品表';
  
INSERT INTO miaosha_goods
VALUES (1, 1, 0.01, 10, '2017-11-05 15:18:00', '2017-11-13 14:00:18'),
       (2, 2, 0.01, 10, '2017-11-05 15:18:00', '2017-11-13 14:00:18');
```
### 创建订单表及秒杀订单表
```mysql
-- 订单表
CREATE TABLE order_info
(
    id               bigint(20) NOT NULL AUTO_INCREMENT,
    user_id          bigint(20)     DEFAULT NULL COMMENT '用户ID',
    goods_id         bigint(20)     DEFAULT NULL COMMENT '商品ID',
    delivery_addr_id bigint(20)     DEFAULT NULL COMMENT '收获地址ID',
    goods_name       varchar(16)    DEFAULT NULL COMMENT '冗余过来的商品名称',
    goods_count      int(11)        DEFAULT 0 COMMENT '商品数量',
    goods_price      decimal(10, 2) DEFAULT 0.00 COMMENT '商品单价',
    order_channel    tinyint(4)     DEFAULT 0 COMMENT '渠道 pc, 2android, 3ios',
    status           tinyint(4)     DEFAULT '0' COMMENT '订单状态,0新建未支付, 1已支付,2已发货, 3已收货, 4已退款,5已完成',
    create_date      datetime       DEFAULT NULL COMMENT '订单的创建时间',
    pay_date         datetime       DEFAULT NULL COMMENT '支付时间',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 12
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';
  
-- 秒杀订单表
CREATE TABLE miaosha_order
(
    id       bigint(20) NOT NULL AUTO_INCREMENT,
    user_id  bigint(20) DEFAULT NULL COMMENT '用户ID',
    order_id bigint(20) DEFAULT NULL COMMENT '订单ID',
    goods_id bigint(20) DEFAULT NULL COMMENT '商品ID',
    PRIMARY KEY (id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4 COMMENT ='秒杀订单表';
```
