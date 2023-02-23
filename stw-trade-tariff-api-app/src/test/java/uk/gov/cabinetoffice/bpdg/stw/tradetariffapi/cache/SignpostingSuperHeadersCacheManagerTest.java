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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils.AppConfigTestUtils.stubCaffeineCacheWithCacheDuration;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils.SignpostingSuperHeadersTestUtils.stubSignpostingSuperHeadersWithComplexMeasures;

import com.google.common.testing.FakeTicker;
import java.time.Duration;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.constants.CacheConstants;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;

@ExtendWith(MockitoExtension.class)
class SignpostingSuperHeadersCacheManagerTest {

  public static final String QUERY_RESULTS_KEY = "key";
  @Mock CacheManager cacheManager;
  @InjectMocks SignpostingSuperHeadersCacheManager signpostingSuperHeadersCacheManager;

  @Test
  @SneakyThrows
  void shouldLookupSignpostingSuperHeadersFromTheCache() {
    // given
    List<SignpostingSuperHeader> signpostingSuperHeaders =
        stubSignpostingSuperHeadersWithComplexMeasures();

    var caffeineCache = stubCaffeineCacheWithCacheDuration("PT1S");
    caffeineCache.put(QUERY_RESULTS_KEY, signpostingSuperHeaders);
    when(cacheManager.getCache(CacheConstants.SUPER_HEADERS_CACHE)).thenReturn(caffeineCache);
    List<Signal<SignpostingSuperHeader>> expectedSignPostingSuperHeadersSignalList =
        Flux.fromIterable(signpostingSuperHeaders).materialize().collectList().block();

    // when
    Mono<List<Signal<SignpostingSuperHeader>>> actualSignPostingSuperHeadersSignalListMono =
        signpostingSuperHeadersCacheManager.lookUpSuperHeadersFromCache(QUERY_RESULTS_KEY);

    // then
    assertThat(expectedSignPostingSuperHeadersSignalList).isNotNull();
    StepVerifier.create(actualSignPostingSuperHeadersSignalListMono)
        .expectNext(expectedSignPostingSuperHeadersSignalList)
        .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldWriteSignpostingSuperHeadersToTheCache() {
    // given
    List<SignpostingSuperHeader> signpostingSuperHeaders =
        stubSignpostingSuperHeadersWithComplexMeasures();
    var caffeineCache = stubCaffeineCacheWithCacheDuration("PT1S");
    when(cacheManager.getCache(CacheConstants.SUPER_HEADERS_CACHE)).thenReturn(caffeineCache);
    List<Signal<SignpostingSuperHeader>> signPostingSuperHeadersSignalList =
        Flux.fromIterable(signpostingSuperHeaders).materialize().collectList().block();

    // when
    Mono<Void> superHeadersToCache =
        signpostingSuperHeadersCacheManager.writeSuperHeadersToCache(
            QUERY_RESULTS_KEY, signPostingSuperHeadersSignalList);

    // then
    StepVerifier.create(superHeadersToCache).verifyComplete();
    @SuppressWarnings("unchecked")
    var expectedSignPostingSuperHeadersList =
        (List<SignpostingSuperHeader>) caffeineCache.get(QUERY_RESULTS_KEY, List.class);
    assertThat(expectedSignPostingSuperHeadersList).isNotEmpty();
  }

  @Test
  @SneakyThrows
  void shouldExpireSuperHeadersCacheAfterOneSecond() {
    // given
    List<SignpostingSuperHeader> signpostingSuperHeaders =
        stubSignpostingSuperHeadersWithComplexMeasures();
    FakeTicker fakeTicker = new FakeTicker();
    var caffeineCache = stubCaffeineCacheWithCacheDuration("PT1S", fakeTicker);
    when(cacheManager.getCache(CacheConstants.SUPER_HEADERS_CACHE)).thenReturn(caffeineCache);
    List<Signal<SignpostingSuperHeader>> signPostingSuperHeadersSignalList =
        Flux.fromIterable(signpostingSuperHeaders).materialize().collectList().block();

    // when
    Mono<Void> superHeadersToCache =
        signpostingSuperHeadersCacheManager.writeSuperHeadersToCache(
            QUERY_RESULTS_KEY, signPostingSuperHeadersSignalList);

    // then
    StepVerifier.create(superHeadersToCache).verifyComplete();
    fakeTicker.advance(Duration.ofSeconds(2));
    @SuppressWarnings("unchecked")
    var expectedSignPostingSuperHeadersList =
        (List<SignpostingSuperHeader>) caffeineCache.get(QUERY_RESULTS_KEY, List.class);
    assertThat(expectedSignPostingSuperHeadersList).isNull();
  }
}
