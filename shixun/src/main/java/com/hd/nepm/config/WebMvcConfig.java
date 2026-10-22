package com.hd.nepm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                // 这三个角色的登录、注册不校验令牌
                .excludePathPatterns(
                        "/admins/login",
                        "/admins/insert",
                        "/gridMember/gridlogin",
                        "/supervisor/SupervisorLogin",
                        "/supervisor/signUp"
                );
    }
}
