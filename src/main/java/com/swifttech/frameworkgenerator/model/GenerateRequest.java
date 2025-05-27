package com.swifttech.frameworkgenerator.model;

import java.util.List;

public class GenerateRequest {
    private String parentProjectName;
    private String parentGroupId;
    private String parentVersion;
    private String outputDirectory;
    private String javaVersion;
    private List<Module> request;

    public String getParentProjectName() {
        return parentProjectName;
    }

    public void setParentProjectName(String parentProjectName) {
        this.parentProjectName = parentProjectName;
    }

    public String getParentGroupId() {
        return parentGroupId;
    }

    public void setParentGroupId(String parentGroupId) {
        this.parentGroupId = parentGroupId;
    }

    public String getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public List<Module> getRequest() {
        return request;
    }

    public void setRequest(List<Module> request) {
        this.request = request;
    }
} 