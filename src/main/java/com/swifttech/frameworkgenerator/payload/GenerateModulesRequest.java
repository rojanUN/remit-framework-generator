package com.swifttech.frameworkgenerator.payload;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

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
}
