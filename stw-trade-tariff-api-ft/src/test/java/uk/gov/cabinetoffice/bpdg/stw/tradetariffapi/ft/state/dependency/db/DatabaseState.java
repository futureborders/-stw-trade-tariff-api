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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.dependency.db;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.DocumentCodeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.FlywaySchemaHistoryRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.MeasureTypeDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingStepChapterAssignmentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingStepCommodityAssignmentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingStepRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingStepSectionAssignmentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingStepTradeTypeAssignmentRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingSuperHeaderDescriptionRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.SignpostingSuperHeaderRepository;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.DocumentCodeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.MeasureTypeDescription;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStep;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStepChapterAssignment;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStepCommodityAssignment;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStepSectionAssignment;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.dao.model.SignpostingStepTradeTypeAssignment;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.ft.state.MockState;

@Component
@Scope("cucumber-glue")
@Data
public class DatabaseState implements MockState {
  @Autowired FlywaySchemaHistoryRepository flywaySchemaHistoryRepository;
  @Autowired SignpostingStepRepository signpostingStepRepository;
  @Autowired SignpostingStepSectionAssignmentRepository signpostingStepSectionAssignmentRepository;

  @Autowired
  SignpostingStepTradeTypeAssignmentRepository signpostingStepTradeTypeAssignmentRepository;

  @Autowired SignpostingStepChapterAssignmentRepository signpostingStepChapterAssignmentRepository;
  @Autowired Flyway flyway;

  @Autowired
  private SignpostingSuperHeaderDescriptionRepository signpostingSuperHeaderDescriptionRepository;

  @Autowired private SignpostingSuperHeaderRepository signpostingSuperHeaderRepository;

  @Autowired DocumentCodeDescriptionRepository documentCodeDescriptionRepository;

  @Autowired MeasureTypeDescriptionRepository measureTypeDescriptionRepository;

  @Autowired
  private SignpostingStepCommodityAssignmentRepository signpostingStepCommodityAssignmentRepository;

  private List<SignpostingStep> steps = new ArrayList<>();
  private List<SignpostingStepSectionAssignment> sectionAssignments = new ArrayList<>();
  private List<SignpostingStepCommodityAssignment> commodityAssignments = new ArrayList<>();
  private List<SignpostingStepTradeTypeAssignment> tradeTypeAssignments = new ArrayList<>();
  private List<SignpostingStepChapterAssignment> chapterAssignments = new ArrayList<>();
  private List<DocumentCodeDescription> documentCodeDescriptions = new ArrayList<>();
  private List<MeasureTypeDescription> measureTypeDescriptions = new ArrayList<>();

  @Override
  public void reset() {
    flywaySchemaHistoryRepository.deleteAll();
    signpostingStepChapterAssignmentRepository.deleteAll();
    signpostingSuperHeaderDescriptionRepository.deleteAll();
    signpostingStepRepository.deleteAll();
    signpostingStepSectionAssignmentRepository.deleteAll();
    signpostingStepCommodityAssignmentRepository.deleteAll();
    documentCodeDescriptionRepository.deleteAll();
    measureTypeDescriptionRepository.deleteAll();
    signpostingSuperHeaderRepository.deleteAll();
  }

  @Override
  public void prime() {
    flyway.migrate();
    signpostingStepRepository.saveAll(steps);
    signpostingStepTradeTypeAssignmentRepository.saveAll(tradeTypeAssignments);
    signpostingStepChapterAssignmentRepository.saveAll(chapterAssignments);

    signpostingStepSectionAssignmentRepository.saveAll(sectionAssignments);
    signpostingStepCommodityAssignmentRepository.saveAll(commodityAssignments);

    measureTypeDescriptionRepository.saveAll(measureTypeDescriptions);
    documentCodeDescriptionRepository.saveAll(documentCodeDescriptions);
  }
}
