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
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JsonProvider;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.datatable.DataTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.ScenarioState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.DependencyState;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.request.GetAdditionalCodesRequestEntity;
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
      "I call the measures API for commodity code (.*) with trade type (.*) and origin country (.*) and destination country (.*) and locale (.*)$")
  public void
  iCallTheMeasuresAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCodeAndLocale(
      String commodityCode,
      String tradeType,
      String originCountryCode,
      String destinationCountryCode,
      String locale) {
    scenarioState.setRequestEntity(
        GetMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .locale(locale)
            .build());
    scenarioState.sendAppRequest();
  }

  @When(
      "I call the measures API with trade date (.*) for commodity code (.*) with trade type (.*) and origin country code (.*) and destination country code (.*)$")
  public void callMeasureAPIWithDateSpecific(
      String tradeDate,
      String commodityCode,
      String tradeType,
      String originCountryCode,
      String destinationCountryCode) {
    scenarioState.setRequestEntity(
        GetMeasuresRequestEntity.builder()
            .commodityCode(commodityCode)
            .importDate(tradeDate)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .build());
    scenarioState.sendAppRequest();
  }

  @When("I call the measures API with (.*) commodity code$")
  public void callRestrictiveMeasuresWithNoQueryParams(String commodityCode) {
    scenarioState.setRequestEntity(
        GetMeasuresRequestEntity.builder().commodityCode(commodityCode).build());
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
      "I call the duties API for commodity code (.*) with trade type (.*) origin country code (.*) destination country code (.*) and locale (.*)")
  public void
      iCallTheDutiesAPIForCommodityCodeWithTradeTypeAndOriginCountryCodeAndDestinationCountryCodeAndLocale(
          String commodityCode,
          String tradeType,
          String originCountryCode,
          String destinationCountryCode,
          String locale) {
    scenarioState.setRequestEntity(
        GetDutiesRequestEntity.builder()
            .commodityCode(commodityCode)
            .tradeType(tradeType)
            .originCountry(originCountryCode)
            .destinationCountry(destinationCountryCode)
            .locale(locale)
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

  @And(
      "the response includes the measures option data below under measure (.*) with measure type series (.*) and measure type (.*)$")
  public void theResponseIncludesTheMeasuresOptionDataBelowUnderMeasure(
      String measureDescription,
      String measureTypeSeries,
      String measureType,
      DataTable dataTable) {
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
    assertThat(measureTypeMapFromResponse.get().get("measureType")).isEqualTo(measureType);

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
                                        option.get("subtype"),
                                        option.get("unit")))
                            .collect(toList()))
                .collect(toList());

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
                        StringUtils.isBlank(option.get("measureOptionSubtype"))
                            ? null
                            : option.get("measureOptionSubtype"),
                        StringUtils.isBlank(option.get("measureOptionUnit"))
                            ? null
                            : option.get("measureOptionUnit")))
            .collect(toList());
    assertThat(measureOptions).contains(expectedOptions);
  }

  @Then(
      "the response includes the prohibition measure data under measure (.*) with measure type series (.*)$")
  public void theResponseIncludesTheProhibitionMeasureDataUnderMeasureWithMeasureTypeSeries(
      String measureId, String measureTypeSeries, DataTable dataTable) {
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());

    List<Map<String, String>> measures =
        JsonPath.read(response, format("$.measures[?(@.id == '%s')]", measureId));
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
              assertThat(nullToEmpty(measureTypeMapFromResponse.get(expected.get("key"))))
                  .isEqualTo(expected.get("value"));
            });
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
    JSONArray responseList = JsonPath.read(response, "$.measures[*].measureType");
    assertThat(responseList).doesNotContain("PROHIBITIVE");
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
                      Tax.builder().text(item.get("text")).value(item.get("value"));
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
            .collect(toList());
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
            .collect(toList());

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
                      Tariff.builder().text(item.get("text")).value(item.get("value"));
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
            .collect(toList());
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
            .collect(toList());

    assertThat(actualTariffs).containsExactlyInAnyOrderElementsOf(expectedTariffs);
  }

  @Given("the below measure type descriptions exist in the content repository")
  public void theBelowMeasureTypeDescriptionsExistInTheContentRepository(DataTable dataTable) {}

  @And("the below document code descriptions exist in the content repository")
  public void theBelowDocumentCodeDescriptionsExistInTheContentRepository(DataTable dataTable) {}

  @Then("the response includes the following measures")
  @SneakyThrows
  public void theResponseIncludesTheFollowingMeasures(DataTable dataTable) {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    List<String> expectedMeasureTypeIds = new ArrayList<>();
    List<String> expectedMeasureTypeDescriptions = new ArrayList<>();
    Map<String, Long> expectedMeasureTypeDescriptionsInstanceCountMap = new HashMap<>();
    for (Map<String, String> columns : rows) {
      expectedMeasureTypeIds.add(columns.get("measureId"));
      expectedMeasureTypeDescriptions.add(columns.get("measureDescriptionOverlay"));
      expectedMeasureTypeDescriptionsInstanceCountMap.put(
          columns.get("measureDescriptionOverlay"), Long.valueOf(columns.get("numberOfInstances")));
    }
    Object response =
        jaywayConfig.jsonProvider().parse(scenarioState.getResponseState().getPayload());
    JSONArray jsonMeasureOptionDescriptionOverlays =
        JsonPath.read(response, "$.measures[*].descriptionOverlay");
    JSONArray jsonIds = JsonPath.read(response, "$.measures[*].id");
    List<String> actualMeasureTypeIds = mapper.readValue(jsonIds.toString(), List.class);
    List<String> actualMeasureOptionDescriptionOverlays =
        mapper.readValue(jsonMeasureOptionDescriptionOverlays.toString(), List.class);
    assertThat(actualMeasureOptionDescriptionOverlays).containsAll(expectedMeasureTypeDescriptions);
    assertThat(actualMeasureTypeIds).containsAll(expectedMeasureTypeIds);
    Map<String, Long> actualMeasureTypeDescriptionsInstanceCountMap =
        actualMeasureOptionDescriptionOverlays.stream()
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    assertThat(actualMeasureTypeDescriptionsInstanceCountMap)
        .containsExactlyInAnyOrderEntriesOf(expectedMeasureTypeDescriptionsInstanceCountMap);
  }

  @EqualsAndHashCode
  @AllArgsConstructor
  @ToString
  private static class MeasureOption {

    private final String type;
    private final String descriptionOverlay;
    private final String certificateCode;
    private final String subtype;
    private final String unit;

    public MeasureOption(
        Object type,
        Object descriptionOverlay,
        Object certificateCode,
        Object subtype,
        Object unit) {
      this.type = Optional.ofNullable(type).map(String::valueOf).orElse(null);
      this.descriptionOverlay =
          Optional.ofNullable(descriptionOverlay).map(String::valueOf).orElse(null);
      this.certificateCode = Optional.ofNullable(certificateCode).map(String::valueOf).orElse(null);
      this.subtype = Optional.ofNullable(subtype).map(String::valueOf).orElse(null);
      this.unit = Optional.ofNullable(unit).map(String::valueOf).orElse(null);
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
