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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi;

import io.micrometer.core.instrument.util.StringUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.Environment;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config.ApplicationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
@EnableCaching
public class Startup {

  private static final Logger log = LoggerFactory.getLogger(Startup.class);

  private final Environment env;

  public Startup(Environment env) {
    this.env = env;
  }

  /**
   * Main method, used to run the application.
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(Startup.class);
    Environment env = app.run(args).getEnvironment();
    logApplicationStartup(env);
  }

  private static void logApplicationStartup(Environment env) {
    String protocol = "http";
    String serverPort = env.getProperty("server.port");
    String contextPath = env.getProperty("server.servlet.context-path");
    if (StringUtils.isBlank(contextPath)) {
      contextPath = "/";
    }
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("The host name could not be determined, using `localhost` as fallback");
    }
    log.info(
        "\n----------------------------------------------------------\n\t"
            + "Application '{}' is running! Access URLs:\n\t"
            + "Local: \t\t{}://localhost:{}{}\n\t"
            + "External: \t{}://{}:{}{}\n\t"
            + "Profile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        protocol,
        serverPort,
        contextPath,
        protocol,
        hostAddress,
        serverPort,
        contextPath,
        env.getActiveProfiles());
  }

  /**
   * Initializes stw-trade-tariff-api.
   *
   * <p>Spring profiles can be configured with a program argument
   * --spring.profiles.active=your-active-profile
   *
   * <p>
   */
  @PostConstruct
  public void initApplication() {
    Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
    if (activeProfiles.contains("dev") && activeProfiles.contains("prod")) {
      log.error(
          "You have misconfigured your application! It should not run "
              + "with both the 'dev' and 'prod' profiles at the same time.");
    }
    if (activeProfiles.contains("dev") && activeProfiles.contains("cloud")) {
      log.error(
          "You have misconfigured your application! It should not "
              + "run with both the 'dev' and 'cloud' profiles at the same time.");
    }
  }
}
