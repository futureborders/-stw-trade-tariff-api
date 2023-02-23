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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.StaticServerHttpHeadersWriter;

@Configuration
@EnableWebFluxSecurity
public class WebSecurityConfiguration {

  // It is necessary to allow inline scripts and styles, and also loading svg blob data for Swagger
  private static final String CSP_HEADER =
    "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' blob: data:;";

  // force HSTS for HTTP
  private static final StaticServerHttpHeadersWriter HSTS_HEADER_WRITER =
    StaticServerHttpHeadersWriter.builder()
      .header("Strict-Transport-Security", "max-age=31536000; includeSubDomains")
      .build();

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http.csrf()
      .disable()
      .anonymous()
      .and()
      .headers()
      .writer(HSTS_HEADER_WRITER)
      .contentSecurityPolicy(CSP_HEADER)
      .and()
      .and()
      .build();
  }
}
