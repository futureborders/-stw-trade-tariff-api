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

import static uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.MeasureOptionType.MULTI_CERTIFICATE;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.DocumentCodeDescription;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MultiCertificateMeasureOption implements MeasureOption {

  private final MeasureOptionType type = MULTI_CERTIFICATE;

  @Getter(AccessLevel.NONE)
  private final DocumentCodeDescription certificate1;

  @Getter(AccessLevel.NONE)
  private final DocumentCodeDescription certificate2;

  @Builder
  public MultiCertificateMeasureOption(
      @NonNull DocumentCodeDescription certificate1,
      @NonNull DocumentCodeDescription certificate2) {
    this.certificate1 = certificate1;
    this.certificate2 = certificate2;
  }

  public String getCertificateCode() {
    return certificate1.getDocumentCode() + " & " + certificate2.getDocumentCode();
  }

  @Override
  public String getDescriptionOverlay() {
    return StringUtils.isNotBlank(certificate1.getDescriptionOverlay())
            && StringUtils.isNotBlank(certificate2.getDescriptionOverlay())
        ? StringUtils.removeEndIgnoreCase(certificate1.getDescriptionOverlay(), ".")
            + " and "
            + StringUtils.replace(
                certificate2.getDescriptionOverlay(),
                StringUtils.split(certificate2.getDescriptionOverlay(), ' ')[0],
                StringUtils.lowerCase(
                    StringUtils.split(certificate2.getDescriptionOverlay(), ' ')[0]))
        : StringUtils.isBlank(certificate2.getDescriptionOverlay())
            ? StringUtils.isNotBlank(certificate1.getDescriptionOverlay())
                ? certificate1.getDescriptionOverlay()
                : null
            : certificate2.getDescriptionOverlay();
  }
}
