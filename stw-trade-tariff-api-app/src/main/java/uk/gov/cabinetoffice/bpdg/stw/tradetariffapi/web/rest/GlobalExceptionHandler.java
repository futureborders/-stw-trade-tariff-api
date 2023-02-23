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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebInputException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.exception.ResourceNotFoundException;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ErrorResponse;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ValidationError;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.web.rest.model.error.ValidationErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
    log.info("Handling ResourceNotFoundException", ex);
    return ErrorResponse.builder().message(ex.getMessage()).build();
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ValidationErrorResponse handleConstraintViolationException(
    ConstraintViolationException ex) {
    log.error("Handling ConstraintViolationException", ex);
    return ValidationErrorResponse.builder()
      .validationErrors(
        ex.getConstraintViolations().stream()
          .map(
            violation ->
              ValidationError.builder()
                .fieldName(
                  violation
                    .getPropertyPath()
                    .toString()
                    .substring(
                      violation.getPropertyPath().toString().indexOf(".") + 1))
                .message(violation.getMessage())
                .build())
          .collect(Collectors.toList()))
      .build();
  }

  @ExceptionHandler(ServerWebInputException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ValidationErrorResponse handleServerWebInputException(ServerWebInputException ex) {
    log.error("Handling ServerWebInputException", ex);
    return ValidationErrorResponse.builder()
      .validationErrors(
        List.of(
          ValidationError.builder()
            .fieldName(Objects.requireNonNull(ex.getMethodParameter()).getParameterName())
            .message(
              ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getReason())
            .build()))
      .build();
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ValidationErrorResponse handleServerWebInputException(ValidationException ex) {
    log.error("Handling ValidationException", ex);
    return ValidationErrorResponse.builder()
        .validationErrors(
            List.of(
                ValidationError.builder()
                    .fieldName(ex.getFieldName())
                    .message(ex.getMessage())
                    .build()))
        .build();
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public final ErrorResponse handleAllExceptions(Exception ex) {
    log.error("Handling Exception", ex);
    return ErrorResponse.builder().message("Unexpected error").build();
  }
}
