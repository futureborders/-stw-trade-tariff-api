Feature: The API should validate inputs and return appropriate validation error responses

  Scenario: When the commodity is not found in DIT trade tariff API
    Given The DIT trade tariff API is returning not found for commodity 1000000000
    When I call the signposting API for commodity code 1000000000 with trade type EXPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 404 response with message Resource 'Commodity' not found with id '1000000000'

  Scenario: When both the import type and the country code is missing
    When I call the signposting API with commodity code 1006107900 but no trade type and no origin country code and no destination country code
    Then I should get the following validation errors
      | fieldName          | message          |
      | tradeType          | must not be null |
      | originCountry      | must not be null |
      | userType           | must not be null |
      | destinationCountry | must not be null |

  Scenario Outline: When mandatory query params are missing
    When I call the signposting API for commodity code <commodity_code> with trade type <trade_type> and origin country code <country_code> and user type <user_type> and destination country code <destination_country>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country  | user_type             | http_status | error_field        | error_message                    |
      | 1006107900     |            | CN           | GB                   | DECLARING_TRADER      | 400         | tradeType          | must not be null                 |
      | 1006107900     | IMPORT     | CN           | GB                   |                       | 400         | userType           | must not be null                 |
      | 1006107900     | DUNNO      | CN           | GB                   | DECLARING_TRADER      | 400         | tradeType          | TradeType 'DUNNO' does not exist |
      | 1006107900     | IMPORT     | CN           | GB                   | DUNNO                 | 400         | userType           | UserType 'DUNNO' does not exist  |
      | 1006107900     | IMPORT     |              | GB                   | NON_DECLARING_TRADER  | 400         | originCountry      | must not be null                 |
      | 1006107900     | IMPORT     | X            | GB                   | DECLARING_TRADER      | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | XYY          | GB                   | INTERMEDIARY          | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | CHINA        | GB                   | INTERMEDIARY          | 400         | originCountry      | must match "^\w{2}$"             |
      | Grapefruit     | IMPORT     | CN           | GB                   | INTERMEDIARY          | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 100610790000   | IMPORT     | CN           | GB                   | INTERMEDIARY          | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 1006107900     | IMPORT     | CN           |                      | INTERMEDIARY          | 400         | destinationCountry | must not be null                 |
      | 1006107900     | IMPORT     | CN           | ABC                  | INTERMEDIARY          | 400         | destinationCountry | Invalid destination country 'ABC'|

  Scenario Outline: When mandatory query params are missing for duties api
    When I call the duties API for commodity code <commodity_code> with trade type <trade_type> and origin country code <country_code> and destination country code <destination_country>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country  | http_status | error_field        | error_message                    |
      | 1006107900     |            | CN           | GB                   | 400         | tradeType          | must not be null                 |
      | 1006107900     | DUNNO      | CN           | GB                   | 400         | tradeType          | TradeType 'DUNNO' does not exist |
      | 1006107900     | IMPORT     |              | GB                   | 400         | originCountry      | must not be null                 |
      | 1006107900     | IMPORT     | X            | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | XYY          | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | CHINA        | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | Grapefruit     | IMPORT     | CN           | GB                   | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 100610790000   | IMPORT     | CN           | GB                   | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 1006107900     | IMPORT     | CN           |                      | 400         | destinationCountry | must not be null                 |
      | 1006107900     | IMPORT     | CN           | ABC                  | 400         | destinationCountry | Invalid destination country 'ABC'|

  Scenario Outline: When mandatory query params are missing for measures api
    When I call the measures API for commodity code <commodity_code> with trade type <trade_type> and origin country code <country_code> and destination country code <destination_country>
    Then I should get a <http_status> response
    Then I should get the following validation errors
      | fieldName     | message         |
      | <error_field> | <error_message> |
    Examples:
      | commodity_code | trade_type | country_code | destination_country  | http_status | error_field        | error_message                    |
      | 1006107900     |            | CN           | GB                   | 400         | tradeType          | must not be null                 |
      | 1006107900     | DUNNO      | CN           | GB                   | 400         | tradeType          | TradeType 'DUNNO' does not exist |
      | 1006107900     | IMPORT     |              | GB                   | 400         | originCountry      | must not be null                 |
      | 1006107900     | IMPORT     | X            | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | XYY          | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | CHINA        | GB                   | 400         | originCountry      | must match "^\w{2}$"             |
      | Grapefruit     | IMPORT     | CN           | GB                   | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 100610790000   | IMPORT     | CN           | GB                   | 400         | commodityCode      | must match "^\d{8}\|\d{10}$"     |
      | 1006107900     | IMPORT     | CN           |                      | 400         | destinationCountry | must not be null                 |
      | 1006107900     | IMPORT     | CN           | ABC                  | 400         | destinationCountry | must match "^\w{2}$"             |
      | 1006107900     | IMPORT     | CN           | HK                   | 400         | destinationCountry | Destination country HK is not a valid UK country |
      | 1006107900     | EXPORT     | CH           | HK                   | 400         | originCountry      | Origin country CH is not a valid UK country |
