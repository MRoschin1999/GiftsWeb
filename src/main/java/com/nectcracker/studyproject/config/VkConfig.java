package com.nectcracker.studyproject.config;

import com.github.scribejava.apis.VkontakteApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VkConfig {

    @Value("${spring.security.oauth2.vk.client.clientId}")
    private String vkClientId;

    @Value("${spring.security.oauth2.vk.client.clientSecret}")
    private String vkClientSecret;

    @Value("${spring.security.oauth2.vk.client.scope}")
    private String vkScope;

    @Value("${spring.security.oauth2.vk.callback}")
    private String vkCallback;

    @Bean
    public OAuth20Service vkScribeBean(){
        return new ServiceBuilder(vkClientId)
                .apiSecret(vkClientSecret)
                .defaultScope(vkScope)
                .callback(vkCallback)
                .build(VkontakteApi.instance());
    }
}
