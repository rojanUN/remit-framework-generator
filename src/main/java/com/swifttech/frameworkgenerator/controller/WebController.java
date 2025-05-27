package com.swifttech.frameworkgenerator.controller;

import com.swifttech.frameworkgenerator.model.Response;
import com.swifttech.frameworkgenerator.payload.GenerateModulesRequest;
import com.swifttech.frameworkgenerator.payload.GenerateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/")
public class WebController {
    private static final Logger logger = LoggerFactory.getLogger(WebController.class);
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
            logger.info("Received project generation request for parent project: {}", request.getParentProjectName());
            
            ResponseEntity<Response> response = restTemplate.postForEntity(
                baseUrl + "/web/generate",
                request,
                Response.class
            );
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Project generation successful");
                model.addAttribute("message", response.getBody().getMessage());
                model.addAttribute("success", response.getBody().isSuccess());
            } else {
                logger.error("Project generation failed with status: {}", response.getStatusCode());
                model.addAttribute("message", "Failed to generate project: " + 
                    (response.getBody() != null ? response.getBody().getMessage() : "Unknown error"));
                model.addAttribute("success", false);
            }
        } catch (RestClientException e) {
            logger.error("Error calling project generation service: {}", e.getMessage(), e);
            model.addAttribute("message", "Error communicating with project generation service: " + e.getMessage());
            model.addAttribute("success", false);
        } catch (Exception e) {
            logger.error("Unexpected error during project generation: {}", e.getMessage(), e);
            model.addAttribute("message", "Unexpected error: " + e.getMessage());
            model.addAttribute("success", false);
        }
        return "result";
    }
} 