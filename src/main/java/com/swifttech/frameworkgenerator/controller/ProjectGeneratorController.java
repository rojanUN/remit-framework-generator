package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.model.Response;
import com.swifttech.frameworkgenerator.model.Module;
import com.swifttech.frameworkgenerator.payload.GenerateModulesRequest;
import com.swifttech.frameworkgenerator.payload.GenerateRequest;
import com.swifttech.frameworkgenerator.service.MultiModuleProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/web/generate")
public class ProjectGeneratorController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectGeneratorController.class);
    private final MultiModuleProjectService projectService;

    public ProjectGeneratorController(MultiModuleProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Response> generateProject(@RequestBody GenerateModulesRequest request) {
        try {
            if (request == null || request.getRequest() == null || request.getRequest().isEmpty()) {
                logger.error("Invalid request: request or request list is empty");
                return ResponseEntity.badRequest()
                    .body(new Response(false, "Invalid request: at least one module is required", null));
            }

            if (request.getParentProjectName() == null || request.getParentProjectName().trim().isEmpty()) {
                logger.error("Invalid request: parent project name is required");
                return ResponseEntity.badRequest()
                    .body(new Response(false, "Parent project name is required", null));
            }

            if (request.getOutputDirectory() == null || request.getOutputDirectory().trim().isEmpty()) {
                logger.error("Invalid request: output directory is required");
                return ResponseEntity.badRequest()
                    .body(new Response(false, "Output directory is required", null));
            }

            List<Module> modules = new ArrayList<>();
            for (GenerateRequest generateRequest : request.getRequest()) {
                if (generateRequest.getModuleName() == null || generateRequest.getModuleName().trim().isEmpty()) {
                    logger.error("Invalid request: module name is required");
                    return ResponseEntity.badRequest()
                        .body(new Response(false, "Module name is required for all modules", null));
                }
                Module module = new Module(generateRequest.getModuleName(), generateRequest.getDependencies());
                modules.add(module);
            }

            logger.info("Generating project with {} modules", modules.size());
            Path outputDirectory = Path.of(request.getOutputDirectory());
            projectService.generateMultiModuleProject(modules, request, outputDirectory);
            return ResponseEntity.ok(new Response(true, "Project generated successfully!", null));
        } catch (IOException e) {
            logger.error("Failed to generate project: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response(false, "Project generation failed: " + e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Unexpected error during project generation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new Response(false, "Unexpected error: " + e.getMessage(), null));
        }
    }
}
