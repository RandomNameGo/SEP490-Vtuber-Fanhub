package com.sep490.vtuber_fanhub.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;

@Configuration
public class PayOSConfig {
    private String clientId = "4012414a-bdce-4204-9041-fbd5452fde10";

    private String apiKey = "0ec3bfde-186d-4161-b40f-ff24dac67142";

    private String checksumKey = "529edc3cc4b752010cf0ead7e3ca044632d6a6b457734f13ca66872b1d51fe50";

    @Bean
    public PayOS payOS() {
        return new PayOS(clientId, apiKey, checksumKey);
    }
}
