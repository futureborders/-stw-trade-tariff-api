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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.cache;

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.constants.CacheConstants.SUPER_HEADERS_CACHE;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;

@Configuration
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class SignpostingSuperHeadersCacheManager {
  private final CacheManager cacheManager;

  @SuppressWarnings("unchecked")
  public Mono<List<Signal<SignpostingSuperHeader>>> lookUpSuperHeadersFromCache(final String key) {
    return Mono.justOrEmpty(
            (Optional.ofNullable(
                (List<SignpostingSuperHeader>)
                    (cacheManager.getCache(SUPER_HEADERS_CACHE).get(key, List.class)))))
        .flatMap(v -> Flux.fromIterable(v).materialize().collectList());
  }

  public Mono<Void> writeSuperHeadersToCache(
      String key, List<Signal<SignpostingSuperHeader>> results) {
    return Flux.fromIterable(results)
        .dematerialize()
        .collectList()
        .doOnNext(l -> cacheManager.getCache(SUPER_HEADERS_CACHE).put(key, l))
        .then();
  }
}
