package com.swifttech.frameworkgenerator.payload;

import lombok.Getter;

import java.util.List;

@Getter
public class GenerateModulesRequest {
    private List<GenerateRequest> request;
    private String parentGroupId;
    private String parentVersion;
    private String outputDirectory;
    private String parentProjectName;
}
