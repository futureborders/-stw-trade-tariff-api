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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.http.HttpStatus.SC_OK;
import static org.awaitility.Awaitility.with;
import static org.awaitility.Durations.TWO_SECONDS;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.response.ResponseEntity;

@RunWith(Cucumber.class)
@CucumberOptions(
    plugin = {
      "pretty",
      "json:build/cucumber-default-json-report.json",
      "junit:build/default-junit-test-report.xml",
      "html:build/cucumber-default-html-report"
    },
    features = "classpath:features",
    glue = "uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.steps",
    tags = {"not @WIP", "not @ignore"},
    strict = true)
@ContextConfiguration(classes = TestConfiguration.class)
@Slf4j
public class TestRunner {

  @BeforeClass
  public static void setUp() {
    log.info("Test setup - initialising context");
    ApplicationContext applicationContext = new AnnotationConfigApplicationContext("uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft");
    log.info("Retrieving the app from the context");
    AppUnderTest app = applicationContext.getBean(AppUnderTest.class);
    log.info("Waiting for application to start");
    with()
        .pollDelay(TWO_SECONDS)
        .pollInterval(TWO_SECONDS)
        .await("Checking if app is up..")
        .atMost(60, SECONDS)
        .until(() -> appIsUp(app));

    log.info("Application started. Applying migrations");
    Flyway flyway = applicationContext.getBean(Flyway.class);
    flyway.migrate();
  }

  private static Boolean appIsUp(AppUnderTest app) {
    try {
      log.info("Checking health endpoint");
      ResponseEntity response = app.health();
      return response.getStatus() == SC_OK;
    } catch (RuntimeException e) {
      return false;
    }
  }
}
