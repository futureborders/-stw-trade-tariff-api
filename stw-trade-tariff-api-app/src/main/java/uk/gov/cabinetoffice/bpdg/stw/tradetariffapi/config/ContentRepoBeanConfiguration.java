/*
 * Copyright 2022 Crown Copyright (Single Trade Window)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.config;

import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.documentcodedescription.DocumentCodeDescriptionContentClient;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.contentclient.measuretypedescription.MeasureTypeDescriptionContentClient;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionContentRepo;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository.MeasureTypeDescriptionRepository;

@Configuration
@Data
@AllArgsConstructor
@Slf4j
public class ContentRepoBeanConfiguration {

  ApplicationProperties applicationProperties;

  @PostConstruct
  void logContentSourceInformation() {
    log.info("Content source is: {} ", applicationProperties.getOverlayContentSource());
  }

  @Bean
  public DocumentCodeDescriptionContentRepo documentCodeDescriptionContentRepo(
      final DocumentCodeDescriptionRepository documentCodeDescriptionRepository,
      final DocumentCodeDescriptionContentClient documentCodeDescriptionContentClient) {
    switch (applicationProperties.getOverlayContentSource()) {
      case "API":
        return documentCodeDescriptionContentClient;
      case "DB":
      default:
        return documentCodeDescriptionRepository;
    }
  }

  @Bean
  public MeasureTypeDescriptionContentRepo measureTypeDescriptionContentRepo(
      final MeasureTypeDescriptionRepository measureTypeDescriptionRepository,
      final MeasureTypeDescriptionContentClient measureTypeDescriptionContentClient) {
    switch (applicationProperties.getOverlayContentSource()) {
      case "API":
        return measureTypeDescriptionContentClient;
      case "DB":
      default:
        return measureTypeDescriptionRepository;
    }
  }
}
