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

import static com.jayway.jsonpath.Configuration.builder;
import static com.jayway.jsonpath.Configuration.defaultConfiguration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JsonProvider;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlProcessor;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@Lazy
@EnableConfigurationProperties({WiremockProperties.class, AppProperties.class})
@Configuration
@ComponentScan(basePackages = {"uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.*"})
@EnableJpaRepositories(
    value = "uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao",
    entityManagerFactoryRef = "emf")
@SuppressWarnings({"unchecked", "lgtm[java/uncaught-number-format-exception]"})
public class TestConfiguration {

  private static final String APP_YML = "application.yml";
  private static final String P_ACTIVE_PROFILE = "spring.profiles.active";
  private static final String P_PROFILES = "spring.profiles";
  private static final String NETWORK = "stw";

  @Autowired private WiremockProperties wiremockProperties;
  @Autowired private AppProperties appProperties;
  @Autowired private DatabaseProperties databaseProperties;

  @Value("${RUNNING_ON_CI:false}")
  private boolean runningOnCi;

  @Value("${PROJECT_SUFFIX:stw-trade-tariff-api}")
  private String projectSuffix;

  @Autowired private DockerConfigClient dockerConfigClient;

  @Bean
  public static PropertySourcesPlaceholderConfigurer properties() {
    PropertySourcesPlaceholderConfigurer config = new PropertySourcesPlaceholderConfigurer();
    YamlPropertiesFactoryBean yamlPropertiesFactoryBean = new YamlPropertiesFactoryBean();
    yamlPropertiesFactoryBean.setDocumentMatchers(documentMatcher());
    yamlPropertiesFactoryBean.setResources(new ClassPathResource(APP_YML));
    config.setProperties(yamlPropertiesFactoryBean.getObject());

    return config;
  }

  private static YamlProcessor.DocumentMatcher documentMatcher() {
    return properties ->
        (System.getProperty(P_ACTIVE_PROFILE, "local").equals(properties.getProperty(P_PROFILES))
            ? YamlProcessor.MatchStatus.FOUND
            : YamlProcessor.MatchStatus.NOT_FOUND);
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    return objectMapper;
  }

  @Bean(destroyMethod = "")
  public WireMock wireMock() {
    return new WireMock(
        runningOnCi
            ? dockerConfigClient.containerIpAddress(
                NETWORK, projectSuffix, "stw-trade-tariff-api-wiremock-ft")
            : wiremockProperties.getHost(),
        runningOnCi
            ? dockerConfigClient.containerPrivatePort(
                NETWORK,
                projectSuffix,
                "stw-trade-tariff-api-wiremock-ft",
                wiremockProperties.getPort())
            : wiremockProperties.getPort(),
        wiremockProperties.getContextPath());
  }

  @Bean
  public JsonProvider jsonProvider() {
    return defaultConfiguration().jsonProvider();
  }

  @Bean
  public AppUnderTest appUnderTest() {
    return new AppUnderTest(
        runningOnCi
            ? dockerConfigClient.containerIpAddress(
                NETWORK, projectSuffix, "stw-trade-tariff-api-app")
            : appProperties.getHost(),
        runningOnCi
            ? dockerConfigClient.containerPrivatePort(
                NETWORK, projectSuffix, "stw-trade-tariff-api-app", appProperties.getPort())
            : appProperties.getPort(),
        httpClient());
  }

  @Bean
  public CloseableHttpClient httpClient() {
    int timeoutMillis = 10000;
    RequestConfig config =
        RequestConfig.custom()
            .setConnectTimeout(500)
            .setConnectionRequestTimeout(200)
            .setSocketTimeout(timeoutMillis)
            .build();
    return HttpClientBuilder.create().setDefaultRequestConfig(config).build();
  }

  @Bean
  public com.jayway.jsonpath.Configuration jaywayConfig() {
    return builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS).build();
  }

  @Bean(name = "emf")
  public LocalContainerEntityManagerFactoryBean EntityManagerFactory() {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource());
    em.setPackagesToScan(new String[] {"uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao"});

    JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);

    return em;
  }

  @Bean
  public DataSource dataSource() {
    Matcher matcher = Pattern.compile("(//)(.*:)(\\d+)(/)").matcher(databaseProperties.getUrl());
    Optional<String> publicPort =
        matcher.find() ? Optional.ofNullable(matcher.group(3)) : Optional.empty();

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(
        runningOnCi
            ? databaseProperties
            .getUrl()
            .replaceFirst(
                "(//)(.*)(:)(\\d+)(/.*)",
                String.format(
                    "$1%s$3%s$5",
                    dockerConfigClient.containerIpAddress(
                        NETWORK, projectSuffix, "stw-trade-tariff-api-postgres-ft"),
                    dockerConfigClient.containerPrivatePort(
                        NETWORK,
                        projectSuffix,
                        "stw-trade-tariff-api-postgres-ft",
                        publicPort
                            .map(Integer::valueOf)
                            .orElseThrow(
                                () ->
                                    new RuntimeException(
                                        String.format(
                                            "Not able to get public port for database from database url '%s'",
                                            databaseProperties.getUrl()))))))
            : databaseProperties.getUrl());
    dataSource.setUsername(databaseProperties.getUser());
    dataSource.setPassword(databaseProperties.getPassword());
    return dataSource;
  }

  @Bean(name = "transactionManager")
  public PlatformTransactionManager dbTransactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(EntityManagerFactory().getObject());
    return transactionManager;
  }
}
