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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.RegExUtils;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class DocumentCodeMeasureOption implements MeasureOption {

  protected String certificateCode;
  protected MeasureOptionType type = MeasureOptionType.CERTIFICATE;
  protected String descriptionOverlay;

  @Getter(AccessLevel.NONE)
  private long totalNumberOfCertificates;

  @Builder
  public DocumentCodeMeasureOption(
      @NonNull DocumentCodeDescription documentCodeDescription, long totalNumberOfCertificates) {
    this.descriptionOverlay = documentCodeDescription.getDescriptionOverlay();
    this.certificateCode = documentCodeDescription.getDocumentCode();
    this.totalNumberOfCertificates = totalNumberOfCertificates;
  }

  @Override
  public String getDescriptionOverlay() {
    return totalNumberOfCertificates > 1
        ? RegExUtils.replaceFirst(descriptionOverlay, "^[Y|y]ou need", "Check if you need")
        : descriptionOverlay;
  }
}
