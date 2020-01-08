package com.lwf.projectpractice.flash_sale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.builder.SpringApplicationBuilder;
//import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

//jar包
@SpringBootApplication
public class MainApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(MainApplication.class, args);
    }
}

//war入口函数
//@SpringBootApplication
//public class MainApplication extends SpringBootServletInitializer {
//
//    public static void main(String[] args) throws Exception {
//        SpringApplication.run(MainApplication.class, args);
//    }
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder){
//        return builder.sources(MainApplication.class);
//    }
//}
