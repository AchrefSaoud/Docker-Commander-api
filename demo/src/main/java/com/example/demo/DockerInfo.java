package com.example.demo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DockerInfo {
    private String kernelVersion;
    private String operatingSystem;
    private String osType;
    private String architecture;
    private int cpus;
    private String totalMemory;
    private String name;
    private String id;
    private String dockerRootDir;
}
