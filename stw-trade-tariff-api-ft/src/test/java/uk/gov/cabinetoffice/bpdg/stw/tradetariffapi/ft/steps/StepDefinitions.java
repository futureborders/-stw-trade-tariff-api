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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.steps;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.request;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.google.common.base.Strings.nullToEmpty;
import static com.jayway.jsonpath.Configuration.defaultConfiguration;
import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JsonProvider;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.config.TestConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.CommodityType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.DocumentCodeDescription.DocumentCodeDescriptionBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.MeasureTypeDescription.MeasureTypeDescriptionBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStep;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStep.SignpostingStepBuilder;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dto.CommodityMeasures;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dto.SignpostingContent;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.ScenarioState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.DependencyState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.GetAdditionalCodesRequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.GetCommodityMeasuresRequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.GetDutiesRequestEntity;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.GetMeasuresRequestEntity;

@ContextConfiguration(classes = TestConfiguration.class)
@Slf4j
public class StepDefinitions {

  final ObjectMapper mapper =
      new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  @Autowired private ScenarioState scenarioState;
  @Autowired private WireMock wireMock;
  @Autowired private DependencyState dependencyState;
  @Autowired private Configuration jaywayConfig;

  @When(
      "I call the signposting API for commodity code (.*) with trade type (.*) and origin country code (.*) and user type (.*) and destination country code (.*)$")
  public void
      iCallTheSignpostingAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndUserTypeAndDestinationCountryCode(
          String commodityCode,
          String tradeType,
          String originCountryCode,
          String userType,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetCommodityMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .userType(userType)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the measures API for commodity code (.*) with trade type (.*) and origin country code (.*) and destination country code (.*)$")
  public void
      iCallTheMeasuresAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCode(
          String commodityCode,
          String tradeType,
          String originCountryCode,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the signposting API for commodity code (.*) with trade type IMPORT and country code CN and user type DECLARING_TRADER and destination country code GB and import date (.*)$")
  public void
      iCallTheSignpostingAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndUserTypeAndDestinationCountryCodeAndImportDate(
          String commodityCode, String importDate) {
    scenarioState.setRequestEntity(
        GetCommodityMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType("IMPORT")
            .originCountry("CN")
            .destinationCountry("GB")
            .userType("DECLARING_TRADER")
            .importDate(importDate)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the signposting API for commodity code (.*) with additional code (.*) and trade type (.*) and origin country code (.*) and user type (.*) and destination country code (GB|XI)$")
  public void
      iCallTheSignpostingAPIForCommodityCodeAndAdditionalCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCode(
          String commodityCode,
          String additionalCode,
          String tradeType,
          String originCountryCode,
          String userType,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetCommodityMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .userType(userType)
            .additionalCode(additionalCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the measures API for commodity code (.*) with additional code (.*) and trade type (.*) and origin country code (.*) and destination country code (GB|XI)$")
  public void
      iCallTheMeasuresAPIForCommodityCodeAndAdditionalCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCode(
          String commodityCode,
          String additionalCode,
          String tradeType,
          String originCountryCode,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .additionalCode(additionalCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the duties API for commodity code (.*) with trade type (.*) and origin country code (.*) and destination country code (.*)$")
  public void
      iCallTheDutiesAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCode(
          String commodityCode,
          String tradeType,
          String originCountryCode,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetDutiesRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the additional codes API for commodity code (.*) with trade type (.*) and origin country code (.*) and destination country code (.*)$")
  public void
      iCallTheAdditionalCodesAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCode(
          String commodityCode,
          String tradeType,
          String originCountryCode,
          String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetAdditionalCodesRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @Then("I should get a (.*) response$")
  public void iShouldGetAResponse(int statusCode) {
    assertThat(scenarioState.getResponseState().getStatus()).isEqualTo(statusCode);
  }

  @Then("the response should contain the following header key and values")
  public void checkResponseHeaders(DataTable expectedHeaders) {
    Map<String, String> responseHeadersMap = scenarioState.getResponseState().getHeaders();
    assertThat(responseHeadersMap).isNotNull();
    expectedHeaders
        .asMaps()
        .forEach(
            expected -> {
              assertThat(responseHeadersMap).containsKey(expected.get("key"));
              assertThat(responseHeadersMap)
                  .containsEntry(expected.get("key"), expected.get("value"));
            });
  }

  @Then("I should get a (.*) response with message (.*)$")
  public void iShouldGetAResponse(int statusCode, String errorMessage) {
    assertThat(scenarioState.getResponseState().getStatus()).isEqualTo(statusCode);
    JsonProvider jsonProvider = defaultConfiguration().jsonProvider();
    Object response = jsonProvider.parse(scenarioState.getResponseState().getPayload());
    assertThat(JsonPath.<String>read(response, "$.message")).isEqualTo(errorMessage);
  }

  @Then("I should get the following validation errors")
  public void iShouldGetTheFollowingValidationErrors(DataTable validationErrors) {

    List<Map<String, String>> validationErrorsMapList =
        validationErrors.asMaps(String.class, String.class);

    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, String>> errors = JsonPath.read(response, "$.validationErrors");
    assertThat(errors).containsExactlyInAnyOrderElementsOf(validationErrorsMapList);
  }

  @SneakyThrows
  @And("The signposting response should contain the following super headers and headers under them")
  public void theSignpostingResponseShouldContainTheFollowingSuperHeadersAndHeaders(
      DataTable expectedHeaders) {
    CommodityMeasures commodityMeasures =
        mapper.readValue(scenarioState.getResponseState().getPayload(), CommodityMeasures.class);
    List<SignpostingContent> signpostingContents = commodityMeasures.getSignpostingContents();
    Map<String, List<String>> superHeadersToHeaders =
        signpostingContents.stream()
            .collect(
                Collectors.toMap(
                    item -> item.getSuperHeader().getDescription(),
                    item ->
                        item.getHeaders().stream()
                            .map(header -> header.getHeader().getDescription())
                            .collect(Collectors.toList())));
    expectedHeaders
        .asMaps()
        .forEach(
            e -> {
              assertThat(superHeadersToHeaders).containsKey(e.get("superheader"));
              assertThat(superHeadersToHeaders.get(e.get("superheader")))
                  .containsExactlyInAnyOrderElementsOf(Arrays.asList(e.get("headers").split(",")));
            });
  }

  @And("The signposting response should contain the following headers")
  public void theSignpostingResponseShouldContainTheFollowingHeaders(DataTable expectedHeaders) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> actualHeaders = JsonPath.read(response, "$..header.description");
    assertThat(actualHeaders).isNotEmpty();
    List<Map<String, String>> expectedHeadersMap = expectedHeaders.asMaps();
    assertThat(actualHeaders).hasSize(expectedHeadersMap.size());

    expectedHeadersMap.forEach(
        e ->
            assertThat(
                    JsonPath.<List<String>>read(
                        response,
                        format(
                            "$..header[?(@.orderIndex==%s && @.description=='%s' && @.explanatoryText=="
                                + (StringUtils.isNotBlank(e.get("explanatoryText"))
                                    ? "'%s'"
                                    : "%snull")
                                + " && @.linkText=='%s' "
                                + " && @.relatedTo=="
                                + (StringUtils.isNotBlank(e.get("relatedTo")) ? "'%s'" : "%snull")
                                + " && @.externalLink=="
                                + (StringUtils.isNotBlank(e.get("externalLink"))
                                    ? "'%s'"
                                    : "%snull")
                                + " )]",
                            e.get("orderIndex"),
                            e.get("description"),
                            StringUtils.trimToEmpty(e.get("explanatoryText")),
                            e.get("linkText"),
                            StringUtils.trimToEmpty(e.get("relatedTo")),
                            StringUtils.trimToEmpty(e.get("externalLink")))))
                .hasSize(1));
  }

  @Given("The DIT trade tariff API is slow to respond")
  public void theVAPIIsSlowToRespond() {
    scenarioState
        .getDependencyState()
        .getTradeTariffServiceState()
        .setPrimedResponse(
            get(urlPathMatching("/api/v2/commodities/(\\d+)$"))
                .willReturn(WireMock.aResponse().withFixedDelay(5000))
                .atPriority(1)
                .build());
  }

  @Given("The DIT trade tariff API is returning 500 errors")
  public void theVAPIIsReturningErrors() {
    scenarioState
        .getDependencyState()
        .getTradeTariffServiceState()
        .setPrimedResponse(
            request("GET", urlPathMatching("/api/v2/commodities/(\\d+)$"))
                .willReturn(
                    ResponseDefinitionBuilder.okForJson("{'message': 'An error has occurred.'}")
                        .withFixedDelay(500)
                        .withStatus(500))
                .atPriority(1)
                .build());
  }

  @Given("^The DIT trade tariff API is returning not found for commodity (\\w{10})$")
  public void theCommodityDoesNotExist(String commodityCode) {
    scenarioState
        .getDependencyState()
        .getTradeTariffServiceState()
        .setPrimedResponse(
            request("GET", urlPathEqualTo("/api/v2/commodities/" + commodityCode))
                .willReturn(
                    ResponseDefinitionBuilder.okForJson(
                            JSONValue.escape("{\"errors\": [{\"detail\": \"404 - Not Found\"}]}"))
                        .withFixedDelay(500)
                        .withStatus(404))
                .atPriority(1)
                .build());
  }

  @And("The DIT API was called with commodity code (.*)")
  public void theDITAPIWasCalled(String commCode) {
    List<LoggedRequest> requests =
        new ArrayList<>(
            wireMock.find(
                new RequestPatternBuilder(
                    new RequestMethod("GET"), urlPathMatching("/api/v2/commodities/(\\d+)$"))));

    assertThat(requests).hasSize(1);
    assertThat(requests.get(0).getUrl()).contains(commCode);
  }

  @When(
      "^I call the signposting API with commodity code (\\w{10}) but no trade type and no origin country code and no destination country code$")
  public void
      iCallTheSignpostingAPIWithCommodityCodeButNoTradeTypeAndNoCountryCodeAndNoDestinationCountryCode(
          String commodityCode) {
    scenarioState.setRequestEntity(
        GetCommodityMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(null)
            .originCountry(null)
            .destinationCountry(null)
            .userType(null)
            .build());
    scenarioState.sendAppRequest();
  }

  @Given("the below signposting step exists in the database")
  public void theBelowSignpostingStepExistsInTheDatabase(DataTable stepData) {
    final List<Map<String, String>> data = stepData.asMaps();
    data.forEach(
        s -> {
          SignpostingStepBuilder signpostingStepBuilder =
              SignpostingStep.builder()
                  .Id(Integer.valueOf(s.get("id")))
                  .headerId(Integer.valueOf(s.get("stepHeaderId")))
                  .description(s.get("stepDescription"))
                  .howToDescription(s.get("stepHowtoDescription"))
                  .url(s.get("stepUrl"))
                  .nonDeclaringTraderContent(s.get("nonDeclaringTraderContent"))
                  .declaringTraderContent(s.get("declaringTraderContent"))
                  .agentContent(s.get("agentContent"))
                  .published(Boolean.parseBoolean(s.getOrDefault("published", "true")));
          if (s.containsKey("destinationCountryRestriction")) {
            signpostingStepBuilder.destinationCountryRestrictions(
                s.get("destinationCountryRestriction").split(","));
          }
          if (s.containsKey("originCountryRestriction")) {
            signpostingStepBuilder.originCountryRestrictions(
                s.get("originCountryRestriction").split(","));
          }
          dependencyState.getDatabaseState().getSteps().add(signpostingStepBuilder.build());
          dependencyState
              .getDatabaseState()
              .getTradeTypeAssignments()
              .add(
                  uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model
                      .SignpostingStepTradeTypeAssignment.builder()
                      .stepId(Integer.valueOf(s.get("id")))
                      .tradeType(TradeType.valueOf(s.get("stepTradeType")))
                      .blanketApply(
                          Boolean.parseBoolean(s.getOrDefault("stepBlanketApply", "false")))
                      .build());
        });
  }

  @Given("the below measure type descriptions exist in the database")
  public void theBelowMeasureTypeDescriptionsExistInTheDatabase(DataTable stepData) {
    final List<Map<String, String>> data = stepData.asMaps();
    data.forEach(
        s -> {
          MeasureTypeDescriptionBuilder measureTypeDescriptionBuilder =
              MeasureTypeDescription.builder()
                  .id(Integer.valueOf(s.get("id")))
                  .measureTypeId(s.get("measureTypeId"))
                  .descriptionOverlay(s.get("descriptionOverlay"))
                  .locale(Locale.valueOf(s.get("locale")))
                  .published(Boolean.parseBoolean(s.getOrDefault("published", "true")));
          if (s.containsKey("destinationCountryRestriction")) {
            measureTypeDescriptionBuilder.destinationCountryRestrictions(
                s.get("destinationCountryRestriction").split(","));
          }
          dependencyState
              .getDatabaseState()
              .getMeasureTypeDescriptions()
              .add(measureTypeDescriptionBuilder.build());
        });
  }

  @SneakyThrows
  @Given("the below document code descriptions exist in the database")
  public void theBelowDocumentCodeDescriptionsExistInTheDatabase(DataTable stepData) {
    final List<Map<String, String>> data = stepData.asMaps();
    data.forEach(
        s -> {
          DocumentCodeDescriptionBuilder documentCodeDescriptionBuilder =
              DocumentCodeDescription.builder()
                  .id(Integer.valueOf(s.get("id")))
                  .documentCode(s.get("documentCode"))
                  .descriptionOverlay(s.get("descriptionOverlay"))
                  .locale(Locale.valueOf(s.get("locale")))
                  .published(Boolean.parseBoolean(s.getOrDefault("published", "true")));
          if (s.containsKey("destinationCountryRestriction")) {
            documentCodeDescriptionBuilder.destinationCountryRestrictions(
                s.get("destinationCountryRestriction").split(","));
          }
          // DB state
          dependencyState
              .getDatabaseState()
              .getDocumentCodeDescriptions()
              .add(documentCodeDescriptionBuilder.build());
        });
  }

  @And("the below step to commodity association exists in the database")
  public void theBelowStepToCommodityAssociationExistsInTheDatabase(DataTable dataTable) {
    final List<Map<String, String>> data = dataTable.asMaps();
    data.forEach(
        s ->
            dependencyState
                .getDatabaseState()
                .getCommodityAssignments()
                .add(
                    uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model
                        .SignpostingStepCommodityAssignment.builder()
                        .stepId(Integer.valueOf(s.get("stepId")))
                        .commodityCode(s.get("commodityCode"))
                        .postgresSQLEnumType(CommodityType.valueOf(s.get("commodityType")))
                        .published(Boolean.parseBoolean(s.getOrDefault("published", "true")))
                        .build()));
  }

  @And("the below step to chapter association exists in the database")
  public void theBelowStepToChapterAssociationExistsInTheDatabase(DataTable dataTable) {
    final List<Map<String, String>> data = dataTable.asMaps();
    data.forEach(
        s ->
            dependencyState
                .getDatabaseState()
                .getChapterAssignments()
                .add(
                    uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model
                        .SignpostingStepChapterAssignment.builder()
                        .stepId(Integer.valueOf(s.get("stepId")))
                        .chapterId(Integer.valueOf(s.get("chapterId")))
                        .published(Boolean.parseBoolean(s.getOrDefault("published", "true")))
                        .build()));
  }

  @And("the below step to section association exists in the database")
  public void theBelowStepToSectionAssociationExistsInTheDatabase(DataTable dataTable) {
    final List<Map<String, String>> data = dataTable.asMaps();
    data.forEach(
        s ->
            dependencyState
                .getDatabaseState()
                .getSectionAssignments()
                .add(
                    uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model
                        .SignpostingStepSectionAssignment.builder()
                        .stepId(Integer.valueOf(s.get("stepId")))
                        .sectionId(Integer.valueOf(s.get("sectionId")))
                        .published(Boolean.parseBoolean(s.getOrDefault("published", "true")))
                        .build()));
  }

  @And("the response includes the step data below")
  public void theResponseContainsStepsDataBelow(DataTable data) {
    List<Map<String, String>> expectedSteps = data.asMaps(String.class, String.class);
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    expectedSteps.forEach(expectedStep -> assertStepExists(response, expectedStep));
  }

  @And("the response does not include the step data below")
  public void theResponseDoesNotIncludeTheStepDataBelow(DataTable data) {
    List<Map<String, String>> expectedSteps = data.asMaps(String.class, String.class);
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    expectedSteps.forEach(expectedStep -> assertStepDoesNotExists(response, expectedStep));
  }

  @And("the response includes the measures option data below under section (.*) and measure (.*)$")
  public void theResponseIncludesTheMeasuresOptionDataBelowUnderSectionAndMeasure(
      String header, String measureDescription, DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> measures =
        JsonPath.read(
            response,
            format(
                "$.signpostingContents[*].headers[?(@.header.description == '%s')].measures[?(@.descriptionOverlay == '%s')]",
                header, measureDescription));
    assertThat(measures).isNotEmpty();

    Optional<Map<String, Object>> measureTypeMapFromResponse =
        measures.stream()
            .filter(
                measureTypeMap ->
                    measureTypeMap.get("descriptionOverlay").equals(measureDescription))
            .findFirst();

    assertThat(measureTypeMapFromResponse).isPresent();

    List<List<MeasureOption>> measureOptions =
        ((JSONArray) measureTypeMapFromResponse.get().get("measureOptions"))
            .stream()
                .map(Map.class::cast)
                .map(
                    measureOption ->
                        (((JSONArray) measureOption.get("options")).stream().map(Map.class::cast))
                            .map(
                                option ->
                                    new MeasureOption(
                                        option.get("type"),
                                        option.get("descriptionOverlay"),
                                        option.get("certificateCode"),
                                        option.get("thresholdDescription")))
                            .collect(Collectors.toList()))
                .collect(Collectors.toList());

    List<MeasureOption> expectedOptions =
        dataTable.asMaps().stream()
            .map(
                option ->
                    new MeasureOption(
                        option.get("measureOptionType"),
                        StringUtils.isBlank(option.get("measureOptionDescriptionOverlay"))
                            ? null
                            : option.get("measureOptionDescriptionOverlay"),

                        StringUtils.isBlank(option.get("measureOptionCertificateCode"))
                            ? null
                            : option.get("measureOptionCertificateCode"),
                        StringUtils.isBlank(option.get("measureOptionThresholdDescription"))
                            ? null
                            : option.get("measureOptionThresholdDescription")))
            .collect(Collectors.toList());
    assertThat(measureOptions).contains(expectedOptions);
  }

  @And(
      "the response includes the measures option data below under measure (.*) with measure type series (.*) and measure type (.*)$")
  public void theResponseIncludesTheMeasuresOptionDataBelowUnderMeasure(
      String measureDescription, String measureTypeSeries, String measureType, DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> measures =
        JsonPath.read(
            response, format("$.measures[?(@.descriptionOverlay == '%s')]", measureDescription));
    assertThat(measures).isNotEmpty();

    Optional<Map<String, Object>> measureTypeMapFromResponse =
        measures.stream()
            .filter(
                measureTypeMap ->
                    measureTypeMap.get("descriptionOverlay").equals(measureDescription))
            .findFirst();

    assertThat(measureTypeMapFromResponse).isPresent();
    assertThat(measureTypeMapFromResponse.get().get("measureTypeSeries"))
        .isEqualTo(measureTypeSeries);
    assertThat(measureTypeMapFromResponse.get().get("measureType"))
        .isEqualTo(measureType);

    List<List<MeasureOption>> measureOptions =
        ((JSONArray) measureTypeMapFromResponse.get().get("measureOptions"))
            .stream()
                .map(Map.class::cast)
                .map(
                    measureOption ->
                        (((JSONArray) measureOption.get("options")).stream().map(Map.class::cast))
                            .map(
                                option ->
                                    new MeasureOption(
                                        option.get("type"),
                                        option.get("descriptionOverlay"),
                                        option.get("certificateCode"),
                                        option.get("thresholdDescription")))
                            .collect(Collectors.toList()))
                .collect(Collectors.toList());

    List<MeasureOption> expectedOptions =
        dataTable.asMaps().stream()
            .map(
                option ->
                    new MeasureOption(
                        option.get("measureOptionType"),
                        StringUtils.isBlank(option.get("measureOptionDescriptionOverlay"))
                            ? null
                            : option.get("measureOptionDescriptionOverlay"),

                        StringUtils.isBlank(option.get("measureOptionCertificateCode"))
                            ? null
                            : option.get("measureOptionCertificateCode"),
                        StringUtils.isBlank(option.get("measureOptionThresholdDescription"))
                            ? null
                            : option.get("measureOptionThresholdDescription")))
            .collect(Collectors.toList());
    assertThat(measureOptions).contains(expectedOptions);
  }

  @Then(
      "the response includes the prohibition measure data under measure (.*) with measure type series (.*)$")
  public void theResponseIncludesTheProhibitionMeasureDataUnderMeasureWithMeasureTypeSeries(
      String measureId, String measureTypeSeries, DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, String>> measures =
        JsonPath.read(
            response, format("$.measures[?(@.id == '%s')]", measureId));
    assertThat(measures).isNotEmpty();
    assertThat(measures).hasSize(1);

    Map<String, String> measureTypeMapFromResponse = measures.get(0);

    assertThat(measureTypeMapFromResponse).containsKey("measureTypeSeries");
    assertThat(measureTypeMapFromResponse.get("measureTypeSeries")).isEqualTo(measureTypeSeries);

    dataTable
        .asMaps()
        .forEach(
            expected -> {
              assertThat(measureTypeMapFromResponse).containsKey(expected.get("key"));
              assertThat(nullToEmpty(measureTypeMapFromResponse.get(expected.get("key")))).isEqualTo(expected.get("value"));
            });
  }

  @And("the response only includes the below measures$")
  public void theResponseIncludesTheMeasuresDataUnderSection(DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> responseList =
        JsonPath.read(response, "$.signpostingContents[*].headers[*].measures[*]");
    assertThat(responseList).isNotEmpty();

    List<String> measureIds =
        dataTable.cells().stream().skip(1).map(m -> m.get(0)).sorted().collect(Collectors.toList());

    List<String> overlayDescriptions =
        dataTable.cells().stream().skip(1).map(m -> m.get(1)).sorted().collect(Collectors.toList());
    assertThat(responseList)
        .hasSize(
            dataTable.asMaps().stream()
                .mapToInt(m -> Integer.parseInt(m.getOrDefault("numberOfInstances", "1")))
                .sum());
    dataTable
        .asMaps()
        .forEach(
            expectedValueMap -> {
              assertThat(
                      responseList.stream()
                          .map(m -> String.valueOf(m.get("id")))
                          .collect(Collectors.toList())
                          .stream()
                          .distinct()
                          .sorted()
                          .collect(Collectors.toList()))
                  .isEqualTo(measureIds);
              assertThat(
                      responseList.stream()
                          .map(m -> String.valueOf(m.get("descriptionOverlay")))
                          .collect(Collectors.toList())
                          .stream()
                          .distinct()
                          .sorted()
                          .collect(Collectors.toList()))
                  .isEqualTo(overlayDescriptions);
            });
  }

  @And("the response should contain the following commodity hierarchy")
  public void theResponseShouldContainTheFollowingCommodityHierarchy(DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, String>> responseList = JsonPath.read(response, "$.commodityHierarchy[*]");
    assertThat(responseList).isNotEmpty();

    for (int i = 0; i < responseList.size(); i++) {
      assertThat(responseList.get(i).equals(dataTable.asMaps().get(i)))
          .withFailMessage(
              format(
                  "The commodity hierarchy item %s found in response does not match with the expected one : %s",
                  responseList.get(i), dataTable.asMaps().get(i)))
          .isTrue();
    }
  }

  @And("the response should contain the measure type {string}")
  public void theResponseShouldContainTheFollowingMeasure(String measureType) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    JSONArray responseList = JsonPath.read(response, "$.measures[*].measureType[*]");
    assertThat(responseList).isNotEmpty();
    long result = responseList.stream().filter(measureType::equals).count();
    assertThat(result).isGreaterThan(0);
  }

  @And("the response shouldn't contain the measure type {string}")
  public void theResponseShouldNotContainTheFollowingMeasure(String measureType) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    JSONArray responseList = JsonPath.read(response, "$.measures[*].measureType[*]");
    assertThat(responseList).isNotEmpty();
    long result = responseList.stream().filter(measureType::equals).count();
    assertThat(result).isZero();
  }

  @Then("the response includes a prohibitions element with the following contents")
  public void theResponseIncludesAProhibitionsElement(DataTable expectedContents) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> prohibitionsList = JsonPath.read(response, "$.prohibitions[*]");
    assertThat(prohibitionsList).isNotEmpty();
    assertThat(prohibitionsList).hasSize(1);
    Map<String, Object> prohibitionsMap = prohibitionsList.get(0);
    expectedContents
        .asMaps()
        .forEach(
            expected -> {
              assertThat(prohibitionsMap).containsKey(expected.get("key"));
              assertThat(prohibitionsMap).containsEntry(expected.get("key"), expected.get("value"));
            });
  }

  @And("the response should contain no prohibitions elements")
  public void theResponseShouldContainNoProhibitionsElements() {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, Object>> prohibitionsList = JsonPath.read(response, "$.prohibitions[*]");
    assertThat(prohibitionsList).isEmpty();
  }

  @And("the response should contain the following additional codes")
  public void theResponseShouldContainTheFollowingAdditionalCodes(DataTable data) {
    List<Map<String, String>> expectedAdditionalCodes = data.asMaps(String.class, String.class);
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    List<Map<String, String>> actual = JsonPath.read(response, "$.data[*]");
    assertThat(actual).hasSize(expectedAdditionalCodes.size());
    expectedAdditionalCodes.forEach(
        expected ->
            assertThat(
                    actual.stream()
                        .filter(
                            m ->
                                m.get("code").equals(expected.get("code"))
                                    && m.get("description").equals(expected.get("description")))
                        .findFirst())
                .withFailMessage("Expected additional %s code not found in response.", expected)
                .isPresent());
  }

  private void assertStepDoesNotExists(Object response, Map<String, String> stepNotExpected) {
    final List<Map<String, String>> stepInResponse = getStepFromResponse(response, stepNotExpected);
    assertThat(stepInResponse)
        .overridingErrorMessage("Unexpected step was returned in response.")
        .isEmpty();
  }

  private void assertStepExists(Object response, Map<String, String> expectedStep) {
    final List<Map<String, String>> stepInResponse = getStepFromResponse(response, expectedStep);
    assertThat(stepInResponse)
        .overridingErrorMessage("Expected step was not returned in response.")
        .isNotEmpty();
  }

  private List<Map<String, String>> getStepFromResponse(
      Object response, Map<String, String> expectedStep) {
    final Filter filter =
        filter(
            where("id")
                .is(expectedStep.get("id"))
                .and("stepDescription")
                .is(expectedStep.get("stepDescription"))
                .and("stepHowtoDescription")
                .is(expectedStep.get("stepHowtoDescription"))
                .and("stepUrl")
                .is(expectedStep.get("stepUrl"))
                .and("nonDeclaringTraderContent")
                .is(expectedStep.get("nonDeclaringTraderContent"))
                .and("declaringTraderContent")
                .is(expectedStep.get("declaringTraderContent"))
                .and("agentContent")
                .is(expectedStep.get("agentContent")));

    return JsonPath.read(response, "$.signpostingContents[*].headers[*].steps[?]", filter);
  }

  @And("the tax and duty should be applicable")
  public void theTaxAndDutyShouldBeApplicable() {
    assertThat(getTaxAndDutyApplicableField()).isTrue();
  }

  @And("the tax and duty should not be applicable")
  public void theTaxAndDutyShouldNotBeApplicable() {
    assertThat(getTaxAndDutyApplicableField()).isFalse();
  }

  private Boolean getTaxAndDutyApplicableField() {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    return JsonPath.read(response, "$.taxAndDuty.applicable");
  }

  @And("the following taxes should be returned")
  public void theFollowingTaxesShouldBeReturned(DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    List<Map<String, Object>> actual = JsonPath.read(response, "$.taxes[*]");

    List<Tax> expectedTaxes =
        dataTable.asMaps().stream()
            .map(
                item -> {
                  Tax.TaxBuilder taxBuilder =
                      Tax.builder()
                          .text(item.get("text"))
                          .value(item.get("value"));
                  if (StringUtils.isNotBlank(item.get("additionalCode"))
                      && StringUtils.isNotBlank(item.get("additionalCodeDescription"))) {
                    taxBuilder.additionalCode(
                        AdditionalCode.builder()
                            .code(item.get("additionalCode"))
                            .description(item.get("additionalCodeDescription"))
                            .build());
                  }
                  return taxBuilder.build();
                })
            .collect(Collectors.toList());
    List<Tax> actualTaxes =
        actual.stream()
            .map(
                item -> {
                  Tax.TaxBuilder taxBuilder =
                      Tax.builder()
                          .text(String.valueOf(item.get("text")))
                          .value(String.valueOf(item.get("value")));
                  if (item.get("additionalCode") != null) {
                    Map<String, String> additionalCode =
                        (Map<String, String>) item.get("additionalCode");
                    taxBuilder.additionalCode(
                        AdditionalCode.builder()
                            .code(additionalCode.get("code"))
                            .description(additionalCode.get("description"))
                            .build());
                  }
                  return taxBuilder.build();
                })
            .collect(Collectors.toList());

    assertThat(actualTaxes).containsExactlyInAnyOrderElementsOf(expectedTaxes);
  }

  @And("the following tariffs should be returned")
  public void theFollowingTariffsShouldBeReturned(DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    List<Map<String, Object>> actual = JsonPath.read(response, "$.tariffs[*]");

    List<Tariff> expectedTariffs =
        dataTable.asMaps().stream()
            .map(
                item -> {
                  Tariff.TariffBuilder tariffBuilder =
                      Tariff.builder()
                          .text(item.get("text"))
                          .value(item.get("value"));
                  if (StringUtils.isNotBlank(item.get("additionalCode"))
                      || StringUtils.isNotBlank(item.get("additionalCodeDescription"))) {
                    tariffBuilder.additionalCode(
                        AdditionalCode.builder()
                            .code(item.get("additionalCode"))
                            .description(item.get("additionalCodeDescription"))
                            .build());
                  }
                  if (StringUtils.isNotBlank(item.get("geographicalAreaId"))
                      || StringUtils.isNotBlank(item.get("geographicalAreaDescription"))) {
                    tariffBuilder.geographicalArea(
                        GeographicalArea.builder()
                            .id(item.get("geographicalAreaId"))
                            .description(item.get("geographicalAreaDescription"))
                            .build());
                  }
                  if (StringUtils.isNotBlank(item.get("quotaNumber"))) {
                    tariffBuilder.quota(Quota.builder().number(item.get("quotaNumber")).build());
                  }
                  return tariffBuilder.build();
                })
            .collect(Collectors.toList());
    List<Tariff> actualTariffs =
        actual.stream()
            .map(
                item -> {
                  Tariff.TariffBuilder tariffBuilder =
                      Tariff.builder()
                          .text(String.valueOf(item.get("text")))
                          .value(String.valueOf(item.get("value")));
                  if (item.get("additionalCode") != null) {
                    Map<String, String> additionalCode =
                        (Map<String, String>) item.get("additionalCode");
                    tariffBuilder.additionalCode(
                        AdditionalCode.builder()
                            .code(additionalCode.get("code"))
                            .description(additionalCode.get("description"))
                            .build());
                  }
                  if (item.get("geographicalArea") != null) {
                    Map<String, String> geographicalArea =
                        (Map<String, String>) item.get("geographicalArea");
                    tariffBuilder.geographicalArea(
                        GeographicalArea.builder()
                            .id(geographicalArea.get("id"))
                            .description(geographicalArea.get("description"))
                            .build());
                  }
                  if (item.get("quota") != null) {
                    Map<String, String> quota = (Map<String, String>) item.get("quota");
                    tariffBuilder.quota(Quota.builder().number(quota.get("number")).build());
                  }
                  return tariffBuilder.build();
                })
            .collect(Collectors.toList());

    assertThat(actualTariffs).containsExactlyInAnyOrderElementsOf(expectedTariffs);
  }

  @EqualsAndHashCode
  @AllArgsConstructor
  @ToString
  private static class MeasureOption {

    private final String type;
    private final String descriptionOverlay;
    private final String certificateCode;
    private final String thresholdDescription;

    public MeasureOption(
        Object type,
        Object descriptionOverlay,
        Object certificateCode,
        Object thresholdDescription) {
      this.type = Optional.ofNullable(type).map(String::valueOf).orElse(null);
      this.descriptionOverlay =
          Optional.ofNullable(descriptionOverlay).map(String::valueOf).orElse(null);
      this.certificateCode = Optional.ofNullable(certificateCode).map(String::valueOf).orElse(null);
      this.thresholdDescription =
          Optional.ofNullable(thresholdDescription).map(String::valueOf).orElse(null);
    }
  }

  @EqualsAndHashCode
  @Builder
  @ToString
  private static class Tax {
    String text;
    String value;
    AdditionalCode additionalCode;
  }

  @EqualsAndHashCode
  @Builder
  @ToString
  private static class Tariff {
    String text;
    String value;
    AdditionalCode additionalCode;
    GeographicalArea geographicalArea;
    Quota quota;
  }

  @EqualsAndHashCode
  @Builder
  @ToString
  private static class AdditionalCode {
    String code;
    String description;
  }

  @EqualsAndHashCode
  @Builder
  @ToString
  private static class GeographicalArea {
    String id;
    String description;
  }

  @EqualsAndHashCode
  @Builder
  @ToString
  private static class Quota {
    String number;
  }
}
