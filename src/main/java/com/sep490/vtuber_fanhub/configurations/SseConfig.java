package com.sep490.vtuber_fanhub.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for Server-Sent Events (SSE)
 * Sets up async timeout for SSE connections to keep them alive longer
 */
@Configuration
public class SseConfig implements WebMvcConfigurer {

    /**
     * Configure async support for SSE connections
     * Sets timeout to 5 minutes to maintain SSE connections
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        // Set timeout to 5 minutes (300000ms) for SSE connections
        configurer.setDefaultTimeout(300000);
    }

    /**
     * Bean for SSE emitter timeout configuration
     * Can be injected where needed for custom timeout handling
     */
    @Bean
    public SseTimeoutConfig sseTimeoutConfig() {
        return new SseTimeoutConfig();
    }

    /**
     * Inner class to hold SSE timeout configuration values
     */
    public static class SseTimeoutConfig {
        // Timeout in milliseconds (5 minutes)
        private final long timeout = 300000;
        
        // Reconnection time in milliseconds (3 seconds)
        private final long reconnectionTime = 3000;

        public long getTimeout() {
            return timeout;
        }

        public long getReconnectionTime() {
            return reconnectionTime;
        }
    }
}
