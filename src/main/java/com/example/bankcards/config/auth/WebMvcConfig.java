package com.example.bankcards.config.auth;

import com.example.bankcards.security.CurrentUserIdArgumentResolver;
import com.example.bankcards.security.OptionalUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;
    private final OptionalUserIdArgumentResolver optionalUserIdArgumentResolver;

    public WebMvcConfig(CurrentUserIdArgumentResolver currentUserIdArgumentResolver,
                        OptionalUserIdArgumentResolver optionalUserIdArgumentResolver) {
        this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
        this.optionalUserIdArgumentResolver = optionalUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
        resolvers.add(optionalUserIdArgumentResolver);
    }
}