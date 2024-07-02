package com.example.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Docker Controller", description = "APIs for managing Docker images and containers")
public class DockerController {

    @GetMapping("/api/images")
    @Operation(summary = "List Docker images", description = "Returns a list of Docker images with their details")
    public List<Map<String, String>> listImages() {
        List<Map<String, String>> images = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("docker images --format \"{{.Repository}}|{{.Tag}}|{{.ID}}|{{.CreatedSince}}|{{.Size}}\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    Map<String, String> image = new HashMap<>();
                    image.put("Repository", parts[0]);
                    image.put("Tag", parts[1]);
                    image.put("ID", parts[2]);
                    image.put("CreatedSince", parts[3]);
                    image.put("Size", parts[4]);
                    images.add(image);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return images;
    }

    @GetMapping("/api/containers")
    @Operation(summary = "List Docker containers", description = "Returns a list of Docker containers with their details")
    public List<Map<String, String>> listContainers() {
        List<Map<String, String>> containers = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("docker ps -a --format \"{{.ID}}|{{.Image}}|{{.Command}}|{{.CreatedAt}}|{{.Status}}|{{.Ports}}|{{.Names}}\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 7) {
                    Map<String, String> container = new HashMap<>();
                    container.put("ID", parts[0]);
                    container.put("Image", parts[1]);
                    container.put("Command", parts[2]);
                    container.put("CreatedAt", parts[3]);
                    container.put("Status", parts[4]);
                    container.put("Ports", parts[5]);
                    container.put("Names", parts[6]);
                    containers.add(container);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return containers;
    }

    @PostMapping("/api/images/{image}")
    @Operation(summary = "Pull a Docker image", description = "Pulls a Docker image from a repository")
    public String pullImage(@Parameter(description = "Name of the Docker image to pull") @PathVariable("image") String image) {
        try {
            Process process = Runtime.getRuntime().exec("docker pull " + image);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return "Image pulled successfully: " + image + "\n" + output.toString();
            } else {
                return "Failed to pull image: " + image + "\n" + output.toString();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error pulling image: " + image + "\n" + e.getMessage();
        }
    }

    @GetMapping("/api/docker-info")
    @Operation(summary = "Get Docker info", description = "Returns information about the Docker daemon")
    public DockerInfo getDockerInfo() {
        DockerInfo dockerInfo = new DockerInfo();
        Map<String, String> infoMap = new HashMap<>();
        try {
            Process process = Runtime.getRuntime().exec("docker info");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    infoMap.put(parts[0].trim(), parts[1].trim());
                }
            }
            process.waitFor();

            dockerInfo.setKernelVersion(infoMap.get("Kernel Version"));
            dockerInfo.setOperatingSystem(infoMap.get("Operating System"));
            dockerInfo.setOsType(infoMap.get("OSType"));
            dockerInfo.setArchitecture(infoMap.get("Architecture"));
            dockerInfo.setCpus(Integer.parseInt(infoMap.getOrDefault("CPUs", "0")));
            dockerInfo.setTotalMemory(infoMap.get("Total Memory"));
            dockerInfo.setName(infoMap.get("Name"));
            dockerInfo.setId(infoMap.get("ID"));
            dockerInfo.setDockerRootDir(infoMap.get("Docker Root Dir"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dockerInfo;
    }

    @DeleteMapping("/api/images/{id}")
    @Operation(summary = "Delete a Docker image", description = "Deletes a Docker image by its ID")
    public String deleteImage(@Parameter(description = "ID of the Docker image to delete") @PathVariable("id") String id) {
        try {
            Process process = Runtime.getRuntime().exec("docker rmi " + id);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Image deleted successfully: " + id + "\n" + output.toString();
            } else {
                return "Failed to delete image: " + id + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error deleting image: " + id + "\n" + e.getMessage();
        }
    }

    @PostMapping("/api/run-container/{image}")
    @Operation(summary = "Run a Docker container", description = "Runs a Docker container from an image")
    public String runContainer(@Parameter(description = "Name of the Docker image to run") @PathVariable("image") String image) {
        try {
            Process process = Runtime.getRuntime().exec("docker run -d " + image);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Container started successfully: " + image + "\nContainer ID: " + output.toString();
            } else {
                return "Failed to start container: " + image + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error starting container: " + image + "\n" + e.getMessage();
        }
    }

    @PostMapping("/api/run-container/{image}/{name}")
    @Operation(summary = "Run a Docker container with name", description = "Runs a Docker container from an image with a specified name")
    public String runContainerWithName(@Parameter(description = "Name of the Docker image to run") @PathVariable("image") String image,
                                       @Parameter(description = "Name to assign to the container") @PathVariable("name") String name) {
        try {
            Process process = Runtime.getRuntime().exec("docker run -d --name " + name + " " + image);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Container started successfully: " + image + "\nContainer ID: " + output.toString();
            } else {
                return "Failed to start container: " + image + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error starting container: " + image + "\n" + e.getMessage();
        }
    }

    @PostMapping("/api/start-container/{name}")
    @Operation(summary = "Start a Docker container by name", description = "Starts a Docker container by its name")
    public String startContainerByName(@Parameter(description = "Name of the Docker container to start") @PathVariable("name") String name) {
        try {
            Process process = Runtime.getRuntime().exec("docker start " + name);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Container started successfully: " + name + "\n" + output.toString();
            } else {
                return "Failed to start container: " + name + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error starting container: " + name + "\n" + e.getMessage();
        }
    }

    @PostMapping("/api/stop-container/{name}")
    @Operation(summary = "Stop a Docker container by name", description = "Stops a Docker container by its name")
    public String stopContainerByName(@Parameter(description = "Name of the Docker container to stop") @PathVariable("name") String name) {
        try {
            Process process = Runtime.getRuntime().exec("docker stop " + name);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Container stopped successfully: " + name + "\n" + output.toString();
            } else {
                return "Failed to stop container: " + name + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error stopping container: " + name + "\n" + e.getMessage();
        }
    }

    @DeleteMapping("/api/delete-container/{name}")
    @Operation(summary = "Delete a Docker container by name", description = "Deletes a Docker container by its name")
    public String deleteContainerByName(@Parameter(description = "Name of the Docker container to delete") @PathVariable("name") String name) {
        try {
            Process process = Runtime.getRuntime().exec("docker rm " + name);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errorOutput = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return "Container deleted successfully: " + name + "\n" + output.toString();
            } else {
                return "Failed to delete container: " + name + "\n" + errorOutput.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error deleting container: " + name + "\n" + e.getMessage();
        }
    }
}
