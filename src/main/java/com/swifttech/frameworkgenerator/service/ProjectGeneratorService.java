package com.swifttech.frameworkgenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProjectGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectGeneratorService.class);
    private final RestTemplate restTemplate;

    public ProjectGeneratorService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public byte[] generateModule(String groupId, String artifactId, String dependencies) {
        try {
            String url = String.format(
                    "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.4.6&baseDir=%s" +
                            "&groupId=%s&artifactId=%s&dependencies=%s",
                    artifactId, groupId, artifactId, dependencies != null ? dependencies : ""
            );
            logger.info("Generating module with URL: {}", url);
            return restTemplate.getForObject(url, byte[].class);
        } catch (RestClientException e) {
            logger.error("Failed to generate module: {}", e.getMessage(), e);
            throw e;
        }
    }
}
