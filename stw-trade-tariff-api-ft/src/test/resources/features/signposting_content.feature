Feature: The API should return the correct signposting content based on given inputs

  Scenario: Call to V2 API should return a 200 status code
    When I call the signposting API for commodity code 1006107900 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And the response should contain the following header key and values
      | key                       | value                                                                                                                |
      | X-Content-Type-Options    | nosniff                                                                                                              |
      | Content-Security-Policy   | default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-inline'; img-src 'self' blob: data:; |
      | Strict-Transport-Security | max-age=31536000; includeSubDomains                                                                                  |
    And The DIT API was called with commodity code 1006107900

  Scenario: Signposting API response should have superheaders and headers assigned
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And The signposting response should contain the following super headers and headers under them
      | superheader                              | headers                                                                                                                                |
      | Before you buy the goods                 | Register your business for importing,Value your goods,Delay or reduce duty payments,Check if you need an import license or certificate |
      | Prepare and submit customs documentation | Check which transportation documents you need,Submit declarations and notifications                                                    |
      | After your goods arrive in the UK        | Claim a VAT refund,Reclaim duty if you've paid the wrong amount,Check which invoices and records you should keep                       |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type INTERMEDIARY and destination country code GB
    Then I should get a 200 response
    And The signposting response should contain the following super headers and headers under them
      | superheader                              | headers                                                                                                                                                              |
      | Before your importer buys the goods      | Get business information from your customer,Check value of goods,Delay or reduce duty payments for your customer,Check if an import license or certificate is needed |
      | Prepare and submit customs documentation | Check which transportation documents are needed,Submit declarations and notifications                                                                                |
      | After the goods arrive in the UK         | Claim a VAT refund,Reclaim duty if your customer has paid the wrong amount,Check which invoices and records your customer should keep                                |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type NON_DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And The signposting response should contain the following super headers and headers under them
      | superheader                              | headers                                                                                                                                |
      | Before you buy the goods                 | Register your business for importing,Value your goods,Delay or reduce duty payments,Check if you need an import license or certificate |
      | Prepare and submit customs documentation | Check which transportation documents you need                                                                                          |
      | After your goods arrive in the UK        | Claim a VAT refund,Reclaim duty if you've paid the wrong amount,Check which invoices and records you should keep                       |

  Scenario: Signposting API response should have correct ordering of headers
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And The signposting response should contain the following headers
      | orderIndex | description                                        | explanatoryText                                                                                                                                                                                                                                                | linkText                              | relatedTo             | externalLink         |
      | 1          | Register your business for importing               |                                                                                                                                                                                                                                                                | Register to bring goods across border | IMPORT_REGISTRATION   |                      |
      | 2          | Value your goods                                   |                                                                                                                                                                                                                                                                | Calculate tax and duty                | CALCULATE_DUTY        |                      |
      | 3          | Delay or reduce duty payments                      |                                                                                                                                                                                                                                                                | Delay duty payments                   | DELAY_DUTY            |                      |
      | 4          | Check if you need an import license or certificate | Certificates can help reduce the cost of duties and make distributing your goods much easier.<br/><br/>All certificates should be obtained before your goods reach the UK border.                                                                              | Licences certificates restrictions    | IMPORT_CONTROLS       |                      |
      | 5          | Check which transportation documents you need      |                                                                                                                                                                                                                                                                | Check information documents           | IMPORT_DOCUMENTATION  |                      |
      | 6          | Submit declarations and notifications              | You need to register to use each of these services if you have not submitted a notification before.<br/><br/>Notifications inform the right government departments that your goods are due to arrive. They are needed to get your goods through the UK border. | Submit declarations notifications     | IMPORT_DECLARATION    | https://externalLink |
      | 7          | Claim a VAT refund                                 |                                                                                                                                                                                                                                                                | Claim back vat customs duties         | CLAIMING_BACK_DUTY    |                      |
      | 8          | Reclaim duty if you\\'ve paid the wrong amount     |                                                                                                                                                                                                                                                                | Reclaim duty                          |                       |                      |
      | 9          | Check which invoices and records you should keep   |                                                                                                                                                                                                                                                                | Keep invoices paperwork               | IMPORT_RECORD_KEEPING |                      |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type NON_DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And The signposting response should contain the following headers
      | orderIndex | description                                        | explanatoryText                                                                                                                                                                   | linkText                              | relatedTo             | externalLink |
      | 1          | Register your business for importing               |                                                                                                                                                                                   | Register to bring goods across border | IMPORT_REGISTRATION   |              |
      | 2          | Value your goods                                   |                                                                                                                                                                                   | Calculate tax and duty                | CALCULATE_DUTY        |              |
      | 3          | Delay or reduce duty payments                      |                                                                                                                                                                                   | Delay duty payments                   | DELAY_DUTY            |              |
      | 4          | Check if you need an import license or certificate | Certificates can help reduce the cost of duties and make distributing your goods much easier.<br/><br/>All certificates should be obtained before your goods reach the UK border. | Licences certificates restrictions    | IMPORT_CONTROLS       |              |
      | 5          | Check which transportation documents you need      |                                                                                                                                                                                   | Check information documents           | IMPORT_DOCUMENTATION  |              |
      | 7          | Claim a VAT refund                                 |                                                                                                                                                                                   | Claim back vat customs duties         | CLAIMING_BACK_DUTY    |              |
      | 8          | Reclaim duty if you\\'ve paid the wrong amount     |                                                                                                                                                                                   | Reclaim duty                          |                       |              |
      | 9          | Check which invoices and records you should keep   |                                                                                                                                                                                   | Keep invoices paperwork               | IMPORT_RECORD_KEEPING |              |


  Scenario: Steps that are assigned to the chapter of the commodity should be returned
    Given the below signposting step exists in the database
      | id | stepDescription                                             | stepHowtoDescription | stepUrl                   | stepTradeType | stepHeaderId | stepBlanketApply |
      | 60 | step mapped to 1006101000                                   | mapped to 1006101000 | http://www.1006101000.com | IMPORT        | 1            | true             |
      | 61 | step mapped to chapter                                      | mapped to chapter    | http://www.chapter.com    | IMPORT        | 1            | false            |
      | 62 | step not yet mapped to chapter, as assignment not published | mapped to chapter    | http://www.chapter.com    | IMPORT        | 1            | false            |
    And the below step to chapter association exists in the database
      | stepId | chapterId | published |
      | 61     | 10        | true      |
      | 62     | 10        | false     |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                   |
      | 60 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com |
      | 61 | step mapped to chapter    | mapped to chapter    | http://www.chapter.com    |
    Then the response does not include the step data below
      | id | stepDescription                                             | stepHowtoDescription | stepUrl                |
      | 62 | step not yet mapped to chapter, as assignment not published | mapped to chapter    | http://www.chapter.com |

  Scenario: Steps that are assigned to the chapter of the commodity based on destination country restrictions should be returned
    Given the below signposting step exists in the database
      | id | stepDescription              | stepHowtoDescription | stepUrl                   | stepTradeType | stepHeaderId | stepBlanketApply | destinationCountryRestriction |
      | 60 | step mapped to 1006101000 GB | mapped to 1006101000 | http://www.1006101000.com | IMPORT        | 1            | true             | GB                            |
      | 61 | step mapped to 1006101000 XI | mapped to 1006101000 | http://www.1006101000.com | IMPORT        | 1            | true             | XI                            |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                   |
      | 60 | step mapped to 1006101000 GB | mapped to 1006101000 | http://www.1006101000.com |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                   |
      | 61 | step mapped to 1006101000 XI | mapped to 1006101000 | http://www.1006101000.com |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code XI
    Then the response includes the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                   |
      | 61 | step mapped to 1006101000 XI | mapped to 1006101000 | http://www.1006101000.com |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                   |
      | 60 | step mapped to 1006101000 GB | mapped to 1006101000 | http://www.1006101000.com |

  Scenario: Steps that are assigned to the chapter of the commodity based on origin country restrictions should be returned
    Given the below signposting step exists in the database
      | id | stepDescription                  | stepHowtoDescription | stepUrl                          | stepTradeType | stepHeaderId | stepBlanketApply | originCountryRestriction |
      | 60 | step mapped to 1006101000 Global | mapped to global     | http://www.1006101000-global.com | IMPORT        | 1            | true             | ALL                      |
      | 61 | step mapped to 1006101000 EU     | mapped to eu         | http://www.1006101000-eu.com     | IMPORT        | 1            | true             | EU                       |
      | 62 | step mapped to 1006101000 FR     | mapped to fr         | http://www.1006101000-fr.com     | IMPORT        | 1            | true             | FR,BE                    |
      | 63 | step mapped to 1006101000 IN     | mapped to in         | http://www.1006101000-in.com     | IMPORT        | 1            | true             | IN                       |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code JP and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription                  | stepHowtoDescription | stepUrl                          |
      | 60 | step mapped to 1006101000 Global | mapped to global     | http://www.1006101000-global.com |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                      |
      | 61 | step mapped to 1006101000 EU | mapped to eu         | http://www.1006101000-eu.com |
      | 62 | step mapped to 1006101000 FR | mapped to fr         | http://www.1006101000-fr.com |
      | 63 | step mapped to 1006101000 IN | mapped to in         | http://www.1006101000-in.com |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code DE and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription                  | stepHowtoDescription | stepUrl                          |
      | 60 | step mapped to 1006101000 Global | mapped to global     | http://www.1006101000-global.com |
      | 61 | step mapped to 1006101000 EU     | mapped to eu         | http://www.1006101000-eu.com     |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                      |
      | 62 | step mapped to 1006101000 FR | mapped to fr         | http://www.1006101000-fr.com |
      | 63 | step mapped to 1006101000 IN | mapped to in         | http://www.1006101000-in.com |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code FR and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription                  | stepHowtoDescription | stepUrl                          |
      | 60 | step mapped to 1006101000 Global | mapped to global     | http://www.1006101000-global.com |
      | 61 | step mapped to 1006101000 EU     | mapped to eu         | http://www.1006101000-eu.com     |
      | 62 | step mapped to 1006101000 FR     | mapped to fr         | http://www.1006101000-fr.com     |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                      |
      | 63 | step mapped to 1006101000 IN | mapped to in         | http://www.1006101000-in.com |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code IN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription                  | stepHowtoDescription | stepUrl                          |
      | 60 | step mapped to 1006101000 Global | mapped to global     | http://www.1006101000-global.com |
      | 63 | step mapped to 1006101000 IN     | mapped to in         | http://www.1006101000-in.com     |
    Then the response does not include the step data below
      | id | stepDescription              | stepHowtoDescription | stepUrl                      |
      | 61 | step mapped to 1006101000 EU | mapped to eu         | http://www.1006101000-eu.com |
      | 62 | step mapped to 1006101000 FR | mapped to fr         | http://www.1006101000-fr.com |

  Scenario: Steps that are assigned to the section of the commodity should be returned
    Given the below signposting step exists in the database
      | id | stepDescription                                             | stepHowtoDescription | stepUrl                   | stepTradeType | stepHeaderId | stepBlanketApply |
      | 60 | step mapped to 1006101000                                   | mapped to 1006101000 | http://www.1006101000.com | IMPORT        | 1            | true             |
      | 61 | step mapped to section                                      | mapped to section    | http://www.section.com    | IMPORT        | 1            | false            |
      | 62 | step not yet mapped to section, as assignment not published | mapped to section    | http://www.section.com    | IMPORT        | 1            | false            |
    And the below step to section association exists in the database
      | stepId | sectionId | published |
      | 61     | 2         | true      |
      | 62     | 2         | false     |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                   |
      | 60 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com |
      | 61 | step mapped to section    | mapped to section    | http://www.section.com    |
    Then the response does not include the step data below
      | id | stepDescription                                             | stepHowtoDescription | stepUrl                |
      | 62 | step not yet mapped to section, as assignment not published | mapped to section    | http://www.section.com |

  Scenario: Steps that are added to a heading or ancestor for EXPORT should not be inherited for IMPORT
    Given the below signposting step exists in the database
      | id | stepDescription                      | stepHowtoDescription | stepUrl                      | stepTradeType | stepHeaderId | stepBlanketApply | published |
      | 60 | step mapped to heading               | heading step         | http://www.doityourself1.com | IMPORT        | 1            | true             | true      |
      | 61 | step mapped to ancestor              | ancestor step        | http://www.ancestor.com      | IMPORT        | 1            | true             | true      |
      | 62 | step mapped to 1006101000            | mapped to 1006101000 | http://www.1006101000.com    | EXPORT        | 1            | true             | true      |
      | 63 | step mapped to heading not published | heading step         | http://www.doityourself1.com | IMPORT        | 1            | true             | false     |
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription         | stepHowtoDescription | stepUrl                      |
      | 60 | step mapped to heading  | heading step         | http://www.doityourself1.com |
      | 61 | step mapped to ancestor | ancestor step        | http://www.ancestor.com      |
    Then the response does not include the step data below
      | id | stepDescription                      | stepHowtoDescription | stepUrl                      |
      | 62 | step mapped to 1006101000            | mapped to 1006101000 | http://www.1006101000.com    |
      | 63 | step mapped to heading not published | heading step         | http://www.doityourself1.com |

  Scenario: Steps that are added to a heading or ancestor for IMPORT should not be inherited for EXPORT
    Given the below signposting step exists in the database
      | id | stepDescription           | stepHowtoDescription | stepUrl                      | stepTradeType | stepHeaderId | stepBlanketApply |
      | 60 | step mapped to heading    | heading step         | http://www.doityourself1.com | IMPORT        | 1            | true             |
      | 61 | step mapped to ancestor   | ancestor step        | http://www.ancestor.com      | IMPORT        | 1            | true             |
      | 62 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com    | EXPORT        | 1            | true             |
    When I call the signposting API for commodity code 1006101000 with trade type EXPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the step data below
      | id | stepDescription           | stepHowtoDescription | stepUrl                   |
      | 62 | step mapped to 1006101000 | mapped to 1006101000 | http://www.1006101000.com |
    Then the response does not include the step data below
      | id | stepDescription         | stepHowtoDescription | stepUrl                      |
      | 60 | step mapped to heading  | heading step         | http://www.doityourself1.com |
      | 61 | step mapped to ancestor | ancestor step        | http://www.ancestor.com      |

  Scenario: Complex measures should be included in certificates or licenses section
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                 | locale | published |
      | 1  | 410           | Veterinary control                 | EN     | true      |
      | 2  | 750           | Import control of organic products | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay                                                                                                        | locale | published |
      | 1  | N853         | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)  | EN     | true      |
      | 2  | C084         | Your goods are for scientific or research usage or for use as diagnostic samples.                                         | EN     | true      |
      | 3  | Y058         | Your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use. | EN     | true      |
      | 4  | C644         | You need a certificate of inspection for organic products                                                                 | EN     | true      |
      | 5  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products)                                            | EN     | true     |
    When I call the signposting API for commodity code 0208907000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Veterinary control
      | measureOptionType     | measureOptionDescriptionOverlay                                                                                                                                                           | measureOptionCertificateCode |
      | CERTIFICATE           | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)                                                                  | N853                         |
      | EXCEPTION             | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.                                                                          | C084                         |
      | THRESHOLD_CERTIFICATE | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and weighs less than 2 kilograms, then your goods are exempt. | Y058                         |
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control of organic products
      | measureOptionType | measureOptionDescriptionOverlay                                                                                | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                      | C644                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products), then your goods are exempt. | Y929                         |


  Scenario: Prohibitions should not be returned in response for a commodity which does not have a prohibition
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And the response should contain no prohibitions elements

  Scenario: Prohibitions should be returned in response for a commodity which has a prohibition
    When I call the signposting API for commodity code 0307111010 with trade type IMPORT and origin country code TR and user type DECLARING_TRADER and destination country code GB
    Then the response includes a prohibitions element with the following contents
      | key               | value                                                      |
      | id                | 277                                                        |
      | measureTypeId     | 277                                                        |
      | measureTypeSeries | A                                                          |
      | measureType       | PROHIBITIVE                                                |
      | legalAct          | A1907950                                                   |
      | description       | ## You are not allowed to import live and chilled bivalve molluscs for human consumption from Turkey%0A%0AThis includes:%0A%0A- clams%0A- mussels%0A- scallops%0A- oysters%0A%0AThere is a ban on importing these goods.%0A%0A[Read the Regulation (EU 743/2103)](https://www.legislation.gov.uk/eur/2013/743) |

  Scenario: Commodity hierarchy should be included in the response
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then I should get a 200 response
    And the response should contain the following commodity hierarchy
      | id         | type      | description                       |
      | 2          | SECTION   | Vegetable products                |
      | 1000000000 | CHAPTER   | Cereals                           |
      | 1006000000 | HEADING   | Rice                              |
      | 1006100000 | COMMODITY | Rice in the husk (paddy or rough) |

  Scenario: Call to signposting API V2 with invalid import date format, should return a 400 status code
    When I call the signposting API for commodity code 1006101000 with trade type IMPORT and country code CN and user type DECLARING_TRADER and destination country code GB and import date 09-09-2021
    Then I should get a 400 response

  Scenario: Call to signposting API V2 valid import date, should get measures specific to that date
    When I call the signposting API for commodity code 0808108090 with trade type IMPORT and country code CN and user type DECLARING_TRADER and destination country code GB and import date 2021-10-07
    Then I should get a 200 response
    And the response should contain the measure type "360"

  Scenario: Call to signposting API V2 valid import date, should get measures specific to that date
    When I call the signposting API for commodity code 0808108090 with trade type IMPORT and country code CN and user type DECLARING_TRADER and destination country code GB and import date 2022-01-01
    Then I should get a 200 response
    And the response shouldn't contain the measure type "360"
