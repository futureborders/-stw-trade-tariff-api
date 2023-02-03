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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;

public final class JsonUtils {
  public static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

  private JsonUtils() {}

  public static String asJsonString(Object object) {
    try {
      return OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T asObject(String json, Class<T> clazz) {
    try {
      return OBJECT_MAPPER.readValue(json, clazz);
    } catch (IOException e) {
      return null;
    }
  }

  private static ObjectMapper createObjectMapper() {
    SimpleModule module = new SimpleModule();
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(module);
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper;
  }
}
