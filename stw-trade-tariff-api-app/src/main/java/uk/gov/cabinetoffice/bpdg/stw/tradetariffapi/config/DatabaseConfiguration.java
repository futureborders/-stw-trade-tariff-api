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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.client.SSLMode;
import io.r2dbc.postgresql.codec.EnumCodec;
import io.r2dbc.spi.ConnectionFactory;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.converter.LocaleConverter;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.converter.TradeTypeConverter;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.converter.UserTypeConverter;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;

@Configuration
public class DatabaseConfiguration extends AbstractR2dbcConfiguration {

  @Value("${SIGNPOSTING_DB_URL:localhost}")
  private String host;

  @Value("${SIGNPOSTING_DB_NAME:signposting}")
  private String dbName;

  @Value("${SIGNPOSTING_DB_USER:signposting_user}")
  private String username;

  @Value("${SIGNPOSTING_DB_PASSWORD:password}")
  private String password;

  @Value("${SSL_MODE:DISABLE}")
  private SSLMode sslMode;

  @Value("${SSL_ROOT_CERT:stw-trade-tariff-api-app/rds-combined-ca-bundle.pem}")
  private String sslRootCert;

  @Value("${STW_SIGNPOSTING_API_DB_CONNECTION_POOL_INITIAL_SIZE:10}")
  private Integer initialSize;

  @Value("${STW_SIGNPOSTING_API_DB_CONNECTION_POOL_MAX_SIZE:25}")
  private Integer maxSize;

  @Value("${STW_SIGNPOSTING_API_DB_CONNECTION_POOL_MAX_IDLE_TIME:5M}")
  private Duration maxIdleTime;

  @Override
  @Bean
  public ConnectionFactory connectionFactory() {
    ConnectionFactory connectionFactory =
        new PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host(host)
                .port(5432)
                .database(dbName)
                .username(username)
                .password(password)
                .sslMode(sslMode)
                .sslRootCert(sslRootCert)
                .codecRegistrar(
                    EnumCodec.builder()
                        .withEnum("trade_type", TradeType.class)
                        .withEnum("user_type", UserType.class)
                        .withEnum("locale", Locale.class)
                        .build())
                .build());
    ConnectionPoolConfiguration configuration =
        ConnectionPoolConfiguration.builder(connectionFactory)
            .initialSize(initialSize)
            .maxSize(maxSize)
            .maxIdleTime(maxIdleTime)
            .build();
    return new ConnectionPool(configuration);
  }

  @Override
  protected List<Object> getCustomConverters() {
    return List.of(new TradeTypeConverter(), new UserTypeConverter(), new LocaleConverter());
  }
}
