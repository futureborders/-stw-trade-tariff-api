/*
 * Copyright 2021 Crown Copyright (Single Trade Window)
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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.cache.CacheFlux;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.cache.SignpostingSuperHeadersCacheManager;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = {@Autowired})
public class SignpostingSuperHeadersContentRepository {

  private final SignpostingSuperHeaderRepository signpostingSuperHeaderRepository;
  private final SignpostingSuperHeadersCacheManager signpostingSuperHeadersCacheManager;

  public Flux<SignpostingSuperHeader> findAllByUserTypeAndLocale(UserType userType, Locale locale) {
    String key = userType.name() + locale.name();
    return CacheFlux.lookup(signpostingSuperHeadersCacheManager::lookUpSuperHeadersFromCache, key)
        .onCacheMissResume(
            signpostingSuperHeaderRepository.findAllByUserTypeAndLocale(userType, locale))
        .andWriteWith(signpostingSuperHeadersCacheManager::writeSuperHeadersToCache);
  }
}
