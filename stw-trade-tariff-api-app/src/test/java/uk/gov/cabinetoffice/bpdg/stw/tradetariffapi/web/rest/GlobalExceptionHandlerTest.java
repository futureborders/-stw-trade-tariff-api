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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ErrorResponse;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

  @InjectMocks GlobalExceptionHandler globalExceptionHandler;

  @Test
  void shouldLogTheResourceNotFoundExceptionAtInfoLevel() {
    // given
    Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
    ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
    listAppender.start();
    logger.addAppender(listAppender);

    // when
    ErrorResponse result =
        globalExceptionHandler.handleResourceNotFoundException(
            new ResourceNotFoundException("Commodity", "8079909999"));

    // then
    List<ILoggingEvent> logsList = listAppender.list;
    assertThat(logsList.get(0).getMessage()).isEqualTo("Handling ResourceNotFoundException");
    assertThat(logsList.get(0).getLevel()).isEqualTo(Level.INFO);
    assertThat(result.getMessage())
        .isEqualTo("Resource 'Commodity' not found with id '8079909999'");
  }
}
