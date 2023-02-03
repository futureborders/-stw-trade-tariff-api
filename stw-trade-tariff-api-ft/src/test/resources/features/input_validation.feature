Feature: The API should validate inputs and return appropriate validation error responses

  Scenario: When the commodity is not found in DIT trade tariff API
    Given The DIT trade tariff API is returning not found for commodity 1000000000
    When I call the measures API for commodity code 1000000000 with trade type EXPORT and origin country code GB and destination country code CN
    Then I should get a 404 response with message Resource 'Commodity' not found with id '1000000000'

 Scenario: When Query Param tradeType null
   When I call the measures API for commodity code 1006107900 with trade type null and origin country code null and destination country code null
   Then I should get the following validation errors
     | fieldName | message                         |
     | tradeType | TradeType 'null' does not exist |

  Scenario: When Query Params tradeType, originCountry and destinationCountry null
    When I call the measures API with 1006107900 commodity code
    Then I should get the following validation errors
      | fieldName          | message          |
      | tradeType          | must not be null |
      | originCountry      | must not be null |
      | destinationCountry | must not be null |

  Scenario Outline: When mandatory query params are missing for duties api
    When I call the duties API for commodity code <commodity_code> with trade type <trade_type> origin country code <country_code> destination country code <destination_country> and locale <locale>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country | locale | http_status | error_field        | error_message                     |
      | 1006107900     |            | CN           | GB                  | EN     | 400         | tradeType          | must not be null                  |
      | 1006107900     | DUNNO      | CN           | GB                  | EN     | 400         | tradeType          | TradeType 'DUNNO' does not exist  |
      | 1006107900     | IMPORT     |              | GB                  | EN     | 400         | originCountry      | must not be null                  |
      | 1006107900     | IMPORT     | X            | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"              |
      | 1006107900     | IMPORT     | XYY          | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"              |
      | 1006107900     | IMPORT     | CHINA        | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"              |
      | Grapefruit     | IMPORT     | CN           | GB                  | EN     | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"      |
      | 100610790000   | IMPORT     | CN           | GB                  | EN     | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"      |
      | 1006107900     | IMPORT     | CN           |                     | EN     | 400         | destinationCountry | must not be null                  |
      | 1006107900     | IMPORT     | CN           | ABC                 | EN     | 400         | destinationCountry | Invalid destination country 'ABC' |
      | 1006107900     | IMPORT     | CN           | GB                  | ABC    | 400         | locale             | Locale 'ABC' does not exist       |

  Scenario Outline: When mandatory query params are missing for measures api
    When I call the measures API for commodity code <commodity_code> with trade type <trade_type> and origin country <country_code> and destination country <destination_country> and locale <locale>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country | locale | http_status | error_field        | error_message                                    |
      | 1006107900     |            | CN           | GB                  | EN     | 400         | tradeType          | must not be null                                 |
      | 1006107900     | DUNNO      | CN           | GB                  | EN     | 400         | tradeType          | TradeType 'DUNNO' does not exist                 |
      | 1006107900     | IMPORT     |              | GB                  | EN     | 400         | originCountry      | must not be null                                 |
      | 1006107900     | IMPORT     | X            | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"                             |
      | 1006107900     | IMPORT     | XYY          | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"                             |
      | 1006107900     | IMPORT     | CHINA        | GB                  | EN     | 400         | originCountry      | must match "^\w{2}$"                             |
      | Grapefruit     | IMPORT     | CN           | GB                  | EN     | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"                     |
      | 100610790000   | IMPORT     | CN           | GB                  | EN     | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"                     |
      | 1006107900     | IMPORT     | CN           |                     | EN     | 400         | destinationCountry | must not be null                                 |
      | 1006107900     | IMPORT     | CN           | ABC                 | EN     | 400         | destinationCountry | must match "^\w{2}$"                             |
      | 1006107900     | IMPORT     | CN           | HK                  | EN     | 400         | destinationCountry | Destination country HK is not a valid UK country |
      | 1006107900     | IMPORT     | CN           | GB                  | XYZ    | 400         | locale             | Locale 'XYZ' does not exist                      |
      | 1006107900     | EXPORT     | CH           | HK                  | EN     | 400         | originCountry      | Origin country CH is not a valid UK country      |
