package com.example.demo.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
@Configuration
public class CloudinaryConfigForImage {
    @Value("${cloudinary.cloud-name1}")
    private String cloudName;

    @Value("${cloudinary.api-key1}")
    private String apiKey;

    @Value("${cloudinary.api-secret1}")
    private String apiSecret;

    @Bean(name = "cloudinaryImage")
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}
