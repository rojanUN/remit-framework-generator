package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.model.Module;
import com.swifttech.frameworkgenerator.service.MultiModuleProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/generate")
public class ProjectGeneratorController {

    private final MultiModuleProjectService projectService;

    public ProjectGeneratorController(MultiModuleProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<String> generateProject() {
        try {
            List<Module> modules = List.of(
                    new Module("module-a", List.of("web", "thymeleaf")),
                    new Module("module-b", List.of("data-jpa", "mysql"))
            );
            projectService.generateMultiModuleProject("com.example", "3.4.6", modules, Path.of("output"));
            return ResponseEntity.ok("Project generated successfully!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate project");
        }
    }
}
