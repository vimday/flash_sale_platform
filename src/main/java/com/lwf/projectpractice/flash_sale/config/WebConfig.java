package com.lwf.projectpractice.flash_sale.config;

import com.lwf.projectpractice.flash_sale.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final UserArgumentResolver userArgumentResolver;
    private final AccessInterceptor accessInterceptor;

    @Autowired
    public WebConfig(UserArgumentResolver userArgumentResolver, AccessInterceptor accessInterceptor) {
        this.userArgumentResolver = userArgumentResolver;
        this.accessInterceptor = accessInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
