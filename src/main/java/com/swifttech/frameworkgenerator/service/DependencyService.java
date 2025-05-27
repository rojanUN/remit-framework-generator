package com.swifttech.frameworkgenerator.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DependencyService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyService.class);
    private final RestTemplate restTemplate;
    private static final String SPRING_INITIALIZR_METADATA_URL = "https://start.spring.io/metadata/client";

    public DependencyService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public Map<String, Object> getMetadata() {
        try {
            logger.info("Fetching metadata from Spring Initializr");
            JsonNode metadata = restTemplate.getForObject(SPRING_INITIALIZR_METADATA_URL, JsonNode.class);
            
            if (metadata == null) {
                logger.error("Failed to fetch metadata: null response");
                return Map.of("dependencies", new TreeMap<>(), "javaVersions", List.of());
            }

            Map<String, Object> result = new HashMap<>();
            
            // Get Java versions
            if (metadata.has("javaVersion") && metadata.get("javaVersion").has("values")) {
                List<JavaVersion> javaVersions = new ArrayList<>();
                metadata.get("javaVersion").get("values").forEach(version -> {
                    if (version.has("id") && version.has("name")) {
                        javaVersions.add(new JavaVersion(
                            version.get("id").asText(),
                            version.get("name").asText()
                        ));
                    }
                });
                result.put("javaVersions", javaVersions);
                logger.info("Found {} Java versions", javaVersions.size());
            } else {
                result.put("javaVersions", List.of());
                logger.warn("No Java versions found in metadata");
            }

            // Get dependencies
            if (metadata.has("dependencies") && metadata.get("dependencies").has("values")) {
                Map<String, List<Dependency>> dependenciesByCategory = new TreeMap<>();
                JsonNode categories = metadata.get("dependencies").get("values");
                AtomicInteger totalDependencies = new AtomicInteger(0);

                categories.forEach(category -> {
                    String categoryName = category.has("name") ? category.get("name").asText() : "Other";
                    if (category.has("values")) {
                        category.get("values").forEach(dep -> {
                            String id = dep.has("id") ? dep.get("id").asText() : "";
                            String name = dep.has("name") ? dep.get("name").asText() : "";
                            String description = dep.has("description") ? dep.get("description").asText() : "";

                            if (!id.isEmpty() && !name.isEmpty()) {
                                dependenciesByCategory
                                    .computeIfAbsent(categoryName, k -> new ArrayList<>())
                                    .add(new Dependency(id, name, description));
                                totalDependencies.incrementAndGet();
                            }
                        });
                    }
                });

                result.put("dependencies", dependenciesByCategory);
                logger.info("Successfully fetched {} dependencies across {} categories", 
                    totalDependencies.get(), dependenciesByCategory.size());
            } else {
                result.put("dependencies", new TreeMap<>());
                logger.error("Failed to fetch dependencies metadata: invalid response structure");
            }

            return result;
        } catch (Exception e) {
            logger.error("Error fetching metadata: {}", e.getMessage(), e);
            return Map.of("dependencies", new TreeMap<>(), "javaVersions", List.of());
        }
    }

    public Map<String, List<Dependency>> getDependencies() {
        Map<String, Object> metadata = getMetadata();
        return (Map<String, List<Dependency>>) metadata.get("dependencies");
    }

    public List<JavaVersion> getJavaVersions() {
        Map<String, Object> metadata = getMetadata();
        return (List<JavaVersion>) metadata.get("javaVersions");
    }

    public static class JavaVersion {
        private final String id;
        private final String name;

        public JavaVersion(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    public static class Dependency {
        private final String id;
        private final String name;
        private final String description;

        public Dependency(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
} 