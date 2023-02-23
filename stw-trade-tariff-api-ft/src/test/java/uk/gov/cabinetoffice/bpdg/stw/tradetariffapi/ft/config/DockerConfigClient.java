// Copyright 2021 Crown Copyright (Single Trade Window)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import org.junit.platform.commons.function.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DockerConfigClient {

  private static final Logger log = LoggerFactory.getLogger(DockerConfigClient.class);

  private final DockerClientConfig config =
      DefaultDockerClientConfig.createDefaultConfigBuilder().build();
  private final DockerHttpClient httpClient =
      new ApacheDockerHttpClient.Builder()
          .dockerHost(config.getDockerHost())
          .sslConfig(config.getSSLConfig())
          .build();
  private DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

  public DockerConfigClient(DockerClient dockerClient) {
    this.dockerClient = dockerClient;
  }

  public DockerConfigClient() {}

  private String inspectContainerIp(String networkId, String containerId) {
    return Try.call(() -> dockerClient.inspectNetworkCmd().withNetworkId(networkId).exec())
        .ifFailure(error -> log.error("Error inspecting container network", error))
        .toOptional()
        .map(
            network -> {
              log.info("Getting container '{}' from network '{}'", containerId, network.getName());
              return network.getContainers().get(containerId);
            })
        .map(
            containerNetworkConfig -> {
              log.info("Container network configuration: {}", containerNetworkConfig);
              return containerNetworkConfig.getIpv4Address().split("/")[0];
            })
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format(
                        "Couldn't retrieve ip address for container '%s' under network '%s'",
                        containerId, networkId)));
  }

  private String networkId(String network, String project, String containerName) {
    return Try.call(() -> dockerClient.listNetworksCmd().exec())
        .ifFailure(error -> log.error("Error retrieving networks", error))
        .toOptional()
        .flatMap(
            networks ->
                networks.stream()
                    .filter(n -> n.getName().contains(project) && n.getName().contains(network))
                    .findFirst()
                    .map(Network::getId))
        .orElseThrow(
            () ->
                new RuntimeException(
                    String.format(
                        "Couldn't retrieve network id for container '%s', project '%s' and network '%s'",
                        containerName, project, network)));
  }

  // TODO: if moved to shared library, add tests
  private String containerIpAddress(String networkId, String containerName) {
    log.info("Retrieving ip for container '{}' on network '{}'", containerName, networkId);
    String ip =
        Try.call(
                () -> {
                  log.info("Inspecting containers");
                  return dockerClient.listContainersCmd().exec();
                })
            .ifFailure(error -> log.error("Error listing containers", error))
            .toOptional()
            .flatMap(
                containers ->
                    containers.stream()
                        .filter(
                            container ->
                                Arrays.asList(container.getNames()).contains("/" + containerName)
                                    && container.getNetworkSettings() != null
                                    && container.getNetworkSettings().getNetworks() != null
                                    && container
                                    .getNetworkSettings()
                                    .getNetworks()
                                    .values()
                                    .stream()
                                    .anyMatch(
                                        network -> networkId.equals(network.getNetworkID())))
                        .map(Container::getId)
                        .map(containerId -> inspectContainerIp(networkId, containerId))
                        .findFirst())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        String.format(
                            "Couldn't retrieve network IP for container '%s' and network id '%s'",
                            containerName, networkId)));
    log.info("Ip '{}' retrieved for container '{}' on network '{}'", ip, containerName, networkId);
    return ip;
  }

  private Integer containerPrivatePort(String networkId, String containerName, Integer publicPort) {
    log.info(
        "Retrieving private port for container '{}' with public port '{}' on network '{}'",
        containerName,
        publicPort,
        networkId);
    Integer privatePort =
        Try.call(
                () -> {
                  log.info("Inspecting containers");
                  return dockerClient.listContainersCmd().exec();
                })
            .ifFailure(error -> log.error("Error listing containers", error))
            .toOptional()
            .map(
                containers ->
                    containers.stream()
                        .map(Container::getPorts)
                        .flatMap(Arrays::stream)
                        .collect(Collectors.toList()))
            .flatMap(
                containerPorts ->
                    containerPorts.stream()
                        .filter(
                            containerPort ->
                                Objects.equals(
                                    containerPort.getPublicPort(), publicPort))
                        .map(ContainerPort::getPrivatePort)
                        .findFirst())
            .orElseThrow(
                () ->
                    new RuntimeException(
                        String.format(
                            "Couldn't retrieve private port for container '%s' with public port '%s' on network '%s'",
                            containerName, publicPort, networkId)));
    log.info(
        "Private port '{}' retrieved for container '{}' with public port '{}' on network '{}'",
        privatePort,
        containerName,
        publicPort,
        networkId);
    return privatePort;
  }

  public String containerIpAddress(String network, String project, String containerName) {
    String networkId = networkId(network, project, containerName);
    return containerIpAddress(networkId, containerName);
  }

  public Integer containerPrivatePort(
      String network, String project, String containerName, Integer publicPort) {
    String networkId = networkId(network, project, containerName);
    return containerPrivatePort(networkId, containerName, publicPort);
  }
}
