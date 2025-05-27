package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.service.DependencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DependencyController {
    private final DependencyService dependencyService;

    public DependencyController(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    @GetMapping("/dependencies")
    public Map<String, List<DependencyService.Dependency>> getDependencies() {
        return dependencyService.getDependencies();
    }

    @GetMapping("/java-versions")
    public List<DependencyService.JavaVersion> getJavaVersions() {
        return dependencyService.getJavaVersions();
    }
} 