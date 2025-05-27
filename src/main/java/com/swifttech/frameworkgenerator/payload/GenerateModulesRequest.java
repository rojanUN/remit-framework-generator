package com.swifttech.frameworkgenerator.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GenerateModulesRequest {
    private List<GenerateRequest> request = new ArrayList<>();
    private String parentGroupId;
    private String parentVersion;
    private String outputDirectory;
    private String parentProjectName;
    private String javaVersion;
}
