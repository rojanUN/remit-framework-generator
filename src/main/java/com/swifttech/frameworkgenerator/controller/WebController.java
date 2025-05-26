package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.model.Response;
import com.swifttech.frameworkgenerator.payload.GenerateModulesRequest;
import com.swifttech.frameworkgenerator.payload.GenerateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/")
public class WebController {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public WebController(RestTemplate restTemplate, 
                        @Value("${app.rest.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @GetMapping
    public String index(Model model) {
        GenerateModulesRequest request = new GenerateModulesRequest();
        request.getRequest().add(new GenerateRequest()); // Add one empty module by default
        model.addAttribute("generateRequest", request);
        return "index";
    }

    @PostMapping("/generate")
    public String generateProject(@ModelAttribute GenerateModulesRequest request, Model model) {
        try {
            ResponseEntity<Response> response = restTemplate.postForEntity(
                baseUrl + "/web/generate",
                request,
                Response.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                model.addAttribute("message", response.getBody().getMessage());
                model.addAttribute("success", response.getBody().isSuccess());
            } else {
                model.addAttribute("message", "Failed to generate project");
                model.addAttribute("success", false);
            }
        } catch (Exception e) {
            model.addAttribute("message", "Error: " + e.getMessage());
            model.addAttribute("success", false);
        }
        return "result";
    }
} 