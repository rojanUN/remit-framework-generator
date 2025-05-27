package com.swifttech.frameworkgenerator.service;

import com.swifttech.frameworkgenerator.model.GenerateRequest;
import com.swifttech.frameworkgenerator.model.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectGeneratorService {
    private static final Logger logger = LoggerFactory.getLogger(ProjectGeneratorService.class);
    private final RestTemplate restTemplate;
    private static final String SPRING_BOOT_VERSION = "3.4.6";

    public ProjectGeneratorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void generateProject(GenerateRequest request) throws IOException {
        logger.info("Generating project with name: {}, groupId: {}, version: {}, javaVersion: {}",
                request.getParentProjectName(), request.getParentGroupId(), request.getParentVersion(), request.getJavaVersion());

        // Validate Java version
        if (request.getJavaVersion() == null || request.getJavaVersion().trim().isEmpty()) {
            throw new IllegalArgumentException("Java version is required");
        }

        // Create parent project
        String parentProjectPath = request.getOutputDirectory() + File.separator + request.getParentProjectName();
        createParentProject(parentProjectPath, request.getParentProjectName(),
                request.getParentGroupId(), request.getParentVersion(), request.getJavaVersion());

        // Generate modules
        for (Module module : request.getRequest()) {
            try {
                generateModule(parentProjectPath, module, request.getParentGroupId(), request.getJavaVersion());
            } catch (Exception e) {
                logger.error("Failed to generate module {}: {}", module.getName(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void createParentProject(String projectPath, String projectName, String groupId,
                                     String version, String javaVersion) throws IOException {
        // Create project directory
        File projectDir = new File(projectPath);
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Failed to create project directory: " + projectPath);
        }

        // Create parent pom.xml
        String parentPomContent = """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                
                    <groupId>%s</groupId>
                    <artifactId>%s</artifactId>
                    <version>%s</version>
                    <packaging>pom</packaging>
                
                    <properties>
                        <java.version>%s</java.version>
                        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
                    </properties>
                
                    <modules>
                    </modules>
                
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                                <configuration>
                                    <excludes>
                                        <exclude>
                                            <groupId>org.projectlombok</groupId>
                                            <artifactId>lombok</artifactId>
                                        </exclude>
                                    </excludes>
                                </configuration>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """.formatted(groupId, projectName, version, javaVersion);

        // Write parent pom.xml
        File parentPomFile = new File(projectPath, "pom.xml");
        Files.writeString(parentPomFile.toPath(), parentPomContent);
    }

    private void generateModule(String parentProjectPath, Module module, String groupId, String javaVersion) throws IOException {
        String modulePath = parentProjectPath + File.separator + module.getName();
        File moduleDir = new File(modulePath);
        if (!moduleDir.exists() && !moduleDir.mkdirs()) {
            throw new IOException("Failed to create module directory: " + modulePath);
        }

        // Generate module using Spring Initializr
        String dependencies = module.getDependencies() != null ? String.join(",", module.getDependencies()) : "";
        String url = String.format(
                "https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=%s&baseDir=%s" +
                        "&groupId=%s&artifactId=%s&dependencies=%s&javaVersion=%s",
                SPRING_BOOT_VERSION,
                module.getName(),
                groupId,
                module.getName(),
                dependencies,
                javaVersion
        );

        logger.info("Generating module with URL: {}", url);
        byte[] moduleZip = restTemplate.getForObject(url, byte[].class);
        if (moduleZip == null) {
            throw new IOException("Failed to generate module: null response from Spring Initializr");
        }

        // Extract the module zip
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(moduleZip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File file = new File(modulePath, entry.getName());
                if (entry.isDirectory()) {
                    if (!file.exists() && !file.mkdirs()) {
                        throw new IOException("Failed to create directory: " + file.getAbsolutePath());
                    }
                } else {
                    Files.copy(zis, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        // Update parent pom.xml to include the new module
        File parentPomFile = new File(parentProjectPath, "pom.xml");
        String parentPomContent = Files.readString(parentPomFile.toPath());
        String moduleEntry = String.format("<module>%s</module>", module.getName());
        if (!parentPomContent.contains(moduleEntry)) {
            parentPomContent = parentPomContent.replace(
                    "<modules>",
                    "<modules>\n        " + moduleEntry
            );
            Files.writeString(parentPomFile.toPath(), parentPomContent);
        }
    }
}
