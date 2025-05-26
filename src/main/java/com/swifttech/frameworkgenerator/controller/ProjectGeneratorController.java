package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.model.Response;
import com.swifttech.frameworkgenerator.model.Module;
import com.swifttech.frameworkgenerator.payload.GenerateModulesRequest;
import com.swifttech.frameworkgenerator.payload.GenerateRequest;
import com.swifttech.frameworkgenerator.service.MultiModuleProjectService;
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

    private final MultiModuleProjectService projectService;

    public ProjectGeneratorController(MultiModuleProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<Response> generateProject(@RequestBody GenerateModulesRequest request) throws IOException {
        try {
            List<Module> modules = new ArrayList<>();
            for (GenerateRequest generateRequest : request.getRequest()) {
                Module module = new Module(generateRequest.getModuleName(), generateRequest.getDependencies());
                modules.add(module);
            }

            Path outputDirectory = Path.of(request.getOutputDirectory());
            projectService.generateMultiModuleProject(modules, request, outputDirectory);
            return ResponseEntity.ok(new Response(true, "Project generated successfully!", null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(false, "Project generation failed!", null));
        }
    }
}
