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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils.SignpostingSuperHeadersTestUtils.stubSignpostingSuperHeadersWithComplexMeasures;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;
import reactor.test.StepVerifier;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.cache.SignpostingSuperHeadersCacheManager;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingSuperHeader;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;

@ExtendWith(MockitoExtension.class)
class SignpostingSuperHeadersContentRepositoryTest {
  @Mock SignpostingSuperHeaderRepository signpostingSuperHeaderRepository;
  @Mock SignpostingSuperHeadersCacheManager signpostingSuperHeadersCacheManager;
  @InjectMocks SignpostingSuperHeadersContentRepository signpostingSuperHeadersContentRepository;

  @Test
  @SneakyThrows
  void shouldReturnCachedSignpostingSuperHeader() {
    // given
    List<SignpostingSuperHeader> signpostingSuperHeaders =
        stubSignpostingSuperHeadersWithComplexMeasures();
    when(signpostingSuperHeadersCacheManager.lookUpSuperHeadersFromCache(anyString()))
        .thenReturn(Flux.fromIterable(signpostingSuperHeaders).materialize().collectList());
    when(signpostingSuperHeaderRepository.findAllByUserTypeAndLocale(
            UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.fromIterable(new ArrayList<>()));

    // when
    Flux<SignpostingSuperHeader> signpostingSuperHeadersFlux =
        signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
            UserType.DECLARING_TRADER, Locale.EN);

    // then
    StepVerifier.create(signpostingSuperHeadersFlux)
        .expectNextSequence(signpostingSuperHeaders)
        .verifyComplete();
  }

  @Test
  @SneakyThrows
  void shouldReturnSignpostingSuperHeadersFromDB() {
    // given
    List<SignpostingSuperHeader> signpostingSuperHeaders =
        stubSignpostingSuperHeadersWithComplexMeasures();
    // noCachedValuesMockingSetup
    List<Signal<SignpostingSuperHeader>> nullListOfSignals = null;
    Mono<List<Signal<SignpostingSuperHeader>>> cachedSignpostingSuperHeaders =
        Mono.justOrEmpty(Optional.empty());
    when(signpostingSuperHeadersCacheManager.lookUpSuperHeadersFromCache(anyString()))
        .thenReturn(cachedSignpostingSuperHeaders);

    when(signpostingSuperHeaderRepository.findAllByUserTypeAndLocale(
            UserType.DECLARING_TRADER, Locale.EN))
        .thenReturn(Flux.fromIterable(signpostingSuperHeaders));

    // writeResultsAfterQueryingRepoMockingSetup
    List<Signal<SignpostingSuperHeader>> signPostingSuperHeadersSignalList =
        Flux.fromIterable(signpostingSuperHeaders).materialize().collectList().block();
    String key = UserType.DECLARING_TRADER.name() + Locale.EN.name();
    when(signpostingSuperHeadersCacheManager.writeSuperHeadersToCache(
            key, signPostingSuperHeadersSignalList))
        .thenReturn(Mono.empty());

    // when
    Flux<SignpostingSuperHeader> signpostingSuperHeadersFlux =
        signpostingSuperHeadersContentRepository.findAllByUserTypeAndLocale(
            UserType.DECLARING_TRADER, Locale.EN);

    // then
    StepVerifier.create(signpostingSuperHeadersFlux)
        .expectNextSequence(signpostingSuperHeaders)
        .verifyComplete();
    verify(signpostingSuperHeaderRepository, times(1))
        .findAllByUserTypeAndLocale(UserType.DECLARING_TRADER, Locale.EN);
  }
}
