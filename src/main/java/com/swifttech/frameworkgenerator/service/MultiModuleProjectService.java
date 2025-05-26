package com.swifttech.frameworkgenerator.service;

import com.swifttech.frameworkgenerator.model.Module;
import com.swifttech.frameworkgenerator.payload.GenerateModulesRequest;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class MultiModuleProjectService {

    private final ProjectGeneratorService projectGeneratorService;

    public MultiModuleProjectService(ProjectGeneratorService projectGeneratorService) {
        this.projectGeneratorService = projectGeneratorService;
    }

    public void generateMultiModuleProject(List<Module> modules, GenerateModulesRequest request, Path outputDir) throws IOException {
        Path parentDir = Files.createDirectories(outputDir.resolve(request.getParentProjectName()));
        createParentPom(parentDir, request.getParentGroupId(), request.getParentVersion(), modules);

        for (Module module : modules) {
            Path moduleDir = Files.createDirectories(parentDir.resolve(module.getName()));
            byte[] moduleZip = projectGeneratorService.generateModule(request.getParentGroupId(), module.getName(), String.join(",", module.getDependencies()));
            unzip(moduleZip, moduleDir);
        }
    }

    private void createParentPom(Path parentDir, String groupId, String version, List<Module> modules) throws IOException {
        StringBuilder modulesSection = new StringBuilder();
        for (Module module : modules) {
            modulesSection.append(String.format("<module>%s</module>\n", module.getName()));
        }
        String parentPom = String.format("""
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>%s</groupId>
                    <artifactId>parent-project</artifactId>
                    <version>%s</version>
                    <packaging>pom</packaging>
                    <modules>
                        %s
                    </modules>
                </project>
                """, groupId, version, modulesSection);
        Files.writeString(parentDir.resolve("pom.xml"), parentPom);
    }

    private void unzip(byte[] zipData, Path targetDir) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(zipData);
             ZipInputStream zis = new ZipInputStream(bis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zis, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}

