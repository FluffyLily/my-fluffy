package com.renee328.front.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 확장자 없는 경로만 처리 → index.html로 넘김
        registry.addViewController("/{path:^(?!api|static|assets|favicon\\.ico|.*\\..*).*$}")
                .setViewName("forward:/index.html");

        registry.addViewController("/{path:^(?!api|static|assets|favicon\\.ico|.*\\..*).*$}/**")
                .setViewName("forward:/index.html");
    }
}
