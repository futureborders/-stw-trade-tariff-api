Feature: Commodities should inherit Signposting steps through the hierarchical association of steps to
  - Commodity Headings
  - Commodity Ancestors

  Background:
    Given the below signposting step exists in the database
      | id | stepDescription                      | stepHowtoDescription | stepUrl                      | stepTradeType | stepHeaderId | published | stepBlanketApply | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading               | heading step         | http://www.doityourself1.com | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
      | 61 | step mapped to ancestor              | ancestor step        | http://www.ancestor.com      | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |
      | 62 | step mapped to 1006101000            | mapped to 1006101000 | http://www.1006101000.com    | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_62 | declaringTraderContent_62 | agentContent_62 |
      | 63 | step mapped to heading not published | heading step         | http://www.doityourself1.com | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_63 | declaringTraderContent_63 | agentContent_63 |
      | 64 | step applicable to all               | common step          | http://www.heading.com       | IMPORT        | 1            | true      | true             | nonDeclaringTraderContent_64 | declaringTraderContent_64 | agentContent_64 |
      | 65 | step mapped to 1109000000            | mapped to 1109000000 | http://www.1109000000.com    | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_65 | declaringTraderContent_65 | agentContent_65 |
      | 66 | step mapped to 1109000001            | mapped to 1109000001 | http://www.1109000001.com    | IMPORT        | 1            | true      | false            | nonDeclaringTraderContent_66 | declaringTraderContent_66 | agentContent_66 |
    And the below step to commodity association exists in the database
      | stepId | commodityCode | commodityType | published |
      | 60     | 1006000000    | HEADING       | true      |
      | 61     | 1006100000    | SUB_HEADING   | true      |
      | 62     | 1006101000    | LEAF          | true      |
      | 63     | 1006000000    | HEADING       | false     |
      | 65     | 1109000000    | HEADING       | true      |
      | 66     | 1109000001    | LEAF          | true      |

  Scenario: Commodities should include any steps associated with their heading and/or ancestors (IMPORT)
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading    | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
      | 61 | step mapped to ancestor   | ancestor step        | http://www.ancestor.com      | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |
      | 62 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com    | nonDeclaringTraderContent_62 | declaringTraderContent_62 | agentContent_62 |
    And the response does not include the step data below
      | id | stepDescription                      | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 63 | step mapped to heading not published | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_63 | declaringTraderContent_63 | agentContent_63 |
    When I call the signposting API for commodity code 1006103000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription         | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading  | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
      | 61 | step mapped to ancestor | ancestor step        | http://www.ancestor.com      | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |
    And the response does not include the step data below
      | id | stepDescription                      | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 62 | step mapped to 1006101000            | mapped to 1006101000 | http://www.1006101000.com    | nonDeclaringTraderContent_62 | declaringTraderContent_62 | agentContent_62 |
      | 63 | step mapped to heading not published | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_63 | declaringTraderContent_63 | agentContent_63 |

  Scenario: Commodities which are also heading should fetch steps associated with their heading only along with steps which apply to all
    When I call the signposting API for commodity code 1109000000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                   | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 64 | step applicable to all    | common step          | http://www.heading.com    | nonDeclaringTraderContent_64 | declaringTraderContent_64 | agentContent_64 |
      | 65 | step mapped to 1109000000 | mapped to 1109000000 | http://www.1109000000.com | nonDeclaringTraderContent_65 | declaringTraderContent_65 | agentContent_65 |
    And the response does not include the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                   | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 66 | step mapped to 1109000001 | mapped to 1109000001 | http://www.1109000001.com | nonDeclaringTraderContent_66 | declaringTraderContent_66 | agentContent_66 |

  Scenario: Commodities with the same Heading but different Ancestors should only share steps associated to the Heading (IMPORT)
    When I call the signposting API for commodity code 1006103000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription         | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading  | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
      | 61 | step mapped to ancestor | ancestor step        | http://www.ancestor.com      | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |
    When I call the signposting API for commodity code 1006201100 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription        | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
    And the response does not include the step data below
      | id | stepDescription         | stepHowtoDescription | stepUrl                 | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 61 | step mapped to ancestor | ancestor step        | http://www.ancestor.com | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |

  Scenario: Commodities with different Heading and Ancestors should not share any steps associated to the other's Headings nor Ancestors (IMPORT)
    When I call the signposting API for commodity code 2804501000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    And the response does not include the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                      | nonDeclaringTraderContent    | declaringTraderContent    | agentContent    |
      | 60 | step mapped to heading    | heading step         | http://www.doityourself1.com | nonDeclaringTraderContent_60 | declaringTraderContent_60 | agentContent_60 |
      | 61 | step mapped to ancestor   | ancestor step        | http://www.ancestor.com      | nonDeclaringTraderContent_61 | declaringTraderContent_61 | agentContent_61 |
      | 62 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com    | nonDeclaringTraderContent_62 | declaringTraderContent_62 | agentContent_62 |
