package com.java.ccs.secondkill.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * @author caocs
 * @date 2021/10/23
 * <p>
 * MVC配置类
 * WebMvcConfigurer配置类其实是Spring内部的一种配置方式，
 * 采用JavaBean的形式来代替传统的xml配置文件形式进行针对框架个性化定制，
 * 可以自定义一些Handler，Interceptor，ViewResolver，MessageConverter。
 * 基于java-based方式的spring mvc配置，需要创建一个配置类并实现WebMvcConfigurer 接口；
 * <p>
 * 在Spring Boot 1.5版本都是靠重写WebMvcConfigurerAdapter的方法来添加自定义拦截器，消息转换器等。
 * 在SpringBoot 2.0 后，该类被标记为@Deprecated（弃用）。
 * 官方推荐直接实现WebMvcConfigurer或者直接继承WebMvcConfigurationSupport
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserArgumentResolver userArgumentResolver;
    @Autowired
    private AccessLimitInterceptor accessLimitInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userArgumentResolver);
    }

    /**
     * 约定大于配置。当我们有WebMvcConfigurer实现时，默认配置静态资源访问路径不再生效。
     * 需要在这里重新制定。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    /**
     * 拦截器，实现校验AccessLimit校验
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessLimitInterceptor);
    }
}
