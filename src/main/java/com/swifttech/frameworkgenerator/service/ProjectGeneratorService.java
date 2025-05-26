package com.swifttech.frameworkgenerator.service;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProjectGeneratorService {

    private final RestTemplate restTemplate;

    public ProjectGeneratorService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public byte[] generateModule(String groupId, String artifactId, String dependencies) {
        String url = String.format(
                "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=3.4.6&baseDir=%s" +
                        "&groupId=%s&artifactId=%s&dependencies=%s",
                artifactId, groupId, artifactId, dependencies
        );
        return restTemplate.getForObject(url, byte[].class);
    }
}
