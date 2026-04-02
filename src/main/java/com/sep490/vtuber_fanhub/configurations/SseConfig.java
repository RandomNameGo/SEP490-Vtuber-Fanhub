package com.sep490.vtuber_fanhub.configurations;

import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SseConfig implements WebMvcConfigurer {


    //Configure async support for SSE connections
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Set timeout to 5 minutes (300000ms) for SSE connections
        configurer.setDefaultTimeout(300000);
    }

    //SSE emitter timeout configuration
    @Bean
    public SseTimeoutConfig sseTimeoutConfig() {
        return new SseTimeoutConfig();
    }

    //Inner class for timeout config
    @Getter
    public static class SseTimeoutConfig {
        // Timeout in 5 minutes
        private final long timeout = 300000;
        
        // Reconnection time in 3 seconds
        private final long reconnectionTime = 3000;

    }
}
