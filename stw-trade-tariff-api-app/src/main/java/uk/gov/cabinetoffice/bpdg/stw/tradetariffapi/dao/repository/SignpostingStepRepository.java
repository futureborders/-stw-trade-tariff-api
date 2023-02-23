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

package uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.repository;

import java.util.List;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.dao.model.SignpostingStep;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.Locale;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.TradeType;
import uk.gov.cabinetoffice.bpdg.stw.tradetariffapi.domain.UserType;

@Repository
public interface SignpostingStepRepository
    extends ReactiveCrudRepository<SignpostingStep, Integer> {

  String SELECT_STEP_FIELDS =
      "SELECT "
          + "step.*,"
          + "true as header,"
          + "header.id as header_id,"
          + "header.related_entity_type as header_related_entity_type, "
          + "header.order_index as header_order_index,"
          + "header_descriptions.header_description, "
          + "header_descriptions.header_explanatory_text, "
          + "header_descriptions.header_link_text, "
          + "header_descriptions.external_link as header_external_link "
          + "from signposting_steps step ";

  String INNER_JOIN_HEADERS =
      " INNER JOIN signposting_step_headers header ON step.header_id = header.id "
          + "INNER JOIN signposting_step_header_descriptions header_descriptions ON header.id = header_descriptions.header_id ";

  String FILTER_HEADERS_BY_USER_TYPE_AND_LOCALE =
      " AND header_descriptions.user_type = CAST(:userType as user_type) AND header_descriptions.locale = CAST(:locale as locale)";

  @Query(
      SELECT_STEP_FIELDS
          + INNER_JOIN_HEADERS
          + " INNER JOIN signposting_step_trade_type_assignment as step_trade_type_assign ON step.id = step_trade_type_assign.signposting_step_id"
          + " WHERE step_trade_type_assign.blanket_apply = true"
          + " AND step_trade_type_assign.trade_type = CAST(:tradeType as trade_type)"
          + " AND step.published = true"
          + FILTER_HEADERS_BY_USER_TYPE_AND_LOCALE)
  Flux<SignpostingStep> findByTradeType(TradeType tradeType, UserType userType, Locale locale);

  @Query(
      SELECT_STEP_FIELDS
          + INNER_JOIN_HEADERS
          + " INNER JOIN signposting_step_trade_type_assignment as step_trade_type_assign ON step.id = step_trade_type_assign.signposting_step_id INNER JOIN signposting_step_commodity_assignment as step_commodity_assign ON step.id = step_commodity_assign.signposting_step_id"
          + " WHERE step_trade_type_assign.blanket_apply = false"
          + " AND step_trade_type_assign.trade_type = CAST(:tradeType as trade_type)"
          + " AND step_commodity_assign.code IN (:commodityHierarchyCodes)"
          + " AND step_commodity_assign.published = true"
          + " AND step.published = true"
          + FILTER_HEADERS_BY_USER_TYPE_AND_LOCALE)
  Flux<SignpostingStep> findByTradeTypeAndCommodityHierarchyCodes(
      TradeType tradeType, List<String> commodityHierarchyCodes, UserType userType, Locale locale);

  @Query(
      SELECT_STEP_FIELDS
          + INNER_JOIN_HEADERS
          + " INNER JOIN signposting_step_trade_type_assignment as step_trade_type_assign ON step.id = step_trade_type_assign.signposting_step_id INNER JOIN signposting_step_section_assignment as step_section_assign ON step.id = step_section_assign.signposting_step_id"
          + " WHERE step_trade_type_assign.blanket_apply = false"
          + " AND step_trade_type_assign.trade_type = CAST(:tradeType as trade_type)"
          + " AND step_section_assign.section_id = :sectionId"
          + " AND step_section_assign.published = true"
          + " AND step.published = true"
          + FILTER_HEADERS_BY_USER_TYPE_AND_LOCALE)
  Flux<SignpostingStep> findByTradeTypeAndSection(
      TradeType tradeType, Integer sectionId, UserType userType, Locale locale);

  @Query(
      SELECT_STEP_FIELDS
          + INNER_JOIN_HEADERS
          + " INNER JOIN signposting_step_trade_type_assignment as step_trade_type_assign ON step.id = step_trade_type_assign.signposting_step_id INNER JOIN signposting_step_chapter_assignment as step_chapter_assign ON step.id = step_chapter_assign.signposting_step_id"
          + " WHERE step_trade_type_assign.blanket_apply = false"
          + " AND step_trade_type_assign.trade_type = CAST(:tradeType as trade_type)"
          + " AND step_chapter_assign.chapter_id = :chapterId"
          + " AND step_chapter_assign.published = true"
          + " AND step.published = true"
          + FILTER_HEADERS_BY_USER_TYPE_AND_LOCALE)
  Flux<SignpostingStep> findByTradeTypeAndChapter(
      TradeType tradeType, Integer chapterId, UserType userType, Locale locale);
}
