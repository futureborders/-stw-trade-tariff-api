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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.DocumentCodeMeasureOption;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DocumentCodeMeasureOptionsTestUtils {

  @SneakyThrows
  public static List<DocumentCodeMeasureOption> stubDocumentCodeMeasureOptions(
      String documentCodeMeasureOptionsFilePath) {
    var objectMapper = new ObjectMapper();
    return objectMapper.readValue(
        new File(documentCodeMeasureOptionsFilePath), new TypeReference<>() {});
  }
}
