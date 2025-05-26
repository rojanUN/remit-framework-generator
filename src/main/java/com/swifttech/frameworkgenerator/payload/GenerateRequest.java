package com.swifttech.frameworkgenerator.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GenerateRequest {

    private String moduleName;
    private String groupId;
    private String version;
    private List<String> dependencies;

}