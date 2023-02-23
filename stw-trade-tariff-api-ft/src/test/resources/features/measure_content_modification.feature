Feature: The API should update the measure option content stored in the CMS based the occurrence of various types of conditions within a measure
  - Modify descriptions when multiple certificates exist
  - Combine descriptions for exception and threshold conditions
  - Return descriptions stand-alone exception as is
  - Combine descriptions for multiple certificates using an 'and'

  Scenario: Simple measure containing one certificate and a threshold
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                 | locale | published |
      | 1  | 750           | Import control of organic products | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay                                                                                             | locale | published |
      | 1  | C644         | You need a certificate of inspection for organic products                                                      | EN     | true      |
      | 2  | Y929         | Your goods are not concerned by Regulation (EC) No 834/2007 (organic products)                                 | EN     | true      |
    When I call the signposting API for commodity code 2204299710 with trade type IMPORT and origin country code AU and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control of organic products
      | measureOptionType | measureOptionDescriptionOverlay                                                                                | measureOptionCertificateCode |
      | CERTIFICATE       | You need a certificate of inspection for organic products                                                      | C644                         |
      | EXCEPTION         | If your goods are not concerned by Regulation (EC) No 834/2007 (organic products), then your goods are exempt. | Y929                         |

  Scenario: Measure containing multiple certificates and a threshold
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                         | locale | published |
      | 1  | 465           | Restriction on entry into free circulation | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay              | locale | published |
      | 1  | C014         | You need a VI1 document         | EN     | true      |
      | 2  | C015         | You need a VI2 extract document | EN     | true      |
    When I call the signposting API for commodity code 2204299710 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Restriction on entry into free circulation
      | measureOptionType | measureOptionDescriptionOverlay                                       | measureOptionCertificateCode |
      | CERTIFICATE       | Check if you need a VI1 document                                      | C014                         |
      | CERTIFICATE       | Check if you need a VI2 extract document                              | C015                         |
      | THRESHOLD         | If your shipment is less than 100 litres, then your goods are exempt. |                              |

  Scenario: Measure containing one certificate and one threshold in which threshold does not match the requirement format
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                         | locale | published |
      | 1  | 465           | Restriction on entry into free circulation | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay                                        | locale | published |
      | 1  | C652         | Accompanying documents for the carriage of wine products  | EN     | true      |
    When I call the signposting API for commodity code 2204299710 with trade type IMPORT and origin country code CH and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Restriction on entry into free circulation
      | measureOptionType | measureOptionDescriptionOverlay                                       | measureOptionCertificateCode |
      | CERTIFICATE       | Accompanying documents for the carriage of wine products              | C652                         |

  Scenario: Complex measure containing one common certificate, one exception and one exception+threshold
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                               | locale | published |
      | 1  | 410           | Veterinary control                               | EN     | true      |
      | 2  | 750           | Import control of organic products NOT PUBLISHED | EN     | false      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay                                                                                                        | locale | published |
      | 1  | N853         | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)  | EN     | true      |
      | 2  | C084         | Your goods are for scientific or research usage or for use as diagnostic samples.                                         | EN     | true      |
      | 3  | Y058         | Your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use. | EN     | true      |
      | 4  | C644         | You need a certificate of inspection for organic products                                                                 | EN     | true      |
    When I call the signposting API for commodity code 0208907000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Veterinary control
      | measureOptionType     | measureOptionDescriptionOverlay                                                                                                                                                           | measureOptionCertificateCode |
      | CERTIFICATE           | You need a Common Health Entry Document for Products of animal origin, germinal products and animal by-products (CHED-P)                                                                  | N853                         |
      | EXCEPTION             | If your goods are for scientific or research usage or for use as diagnostic samples, then your goods are exempt.                                                                          | C084                         |
      | THRESHOLD_CERTIFICATE | If your shipment contains goods which are for passengers' personal luggage and are intended for personal consumption or use and weighs less than 2 kilograms, then your goods are exempt. | Y058                         |

  Scenario: For a complex measure containing a common certificate, common threshold, common exception and disjoint certificates, the disjoint certificates should be combined into a single measure option
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay       | locale | published |
      | 1  | 755           | Import control for waste | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay                                                                                                                          | locale | published |
      | 1  | C672         | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU                                   | EN     | true      |
      | 2  | C669         | You need Notification document for import/export of hazardous or mixed notifiable waste.                                                    | EN     | true      |
      | 3  | Y923         | Your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law.                                | EN     | true      |
      | 4  | C670         | You need a movement document for import/export of hazardous or mixed notifiable waste.                                                      | EN     | true      |
    When I call the signposting API for commodity code 2529220000 with trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control for waste
      | measureOptionType | measureOptionDescriptionOverlay                                                                                                                                                    | measureOptionCertificateCode |
      | THRESHOLD         | If your shipment weighs less than 20 kilograms, then your goods are exempt.                                                                                                        |                              |
      | CERTIFICATE       | You need information document for export of non-hazardous waste or imports of non-hazardous waste from EU                                                                          | C672                         |
      | EXCEPTION         | If your goods Products not considered as waste according to Regulation (EC) No 1013/2006 as retained in UK law, then your goods are exempt.                                        | Y923                         |
      | MULTI_CERTIFICATE | You need Notification document for import/export of hazardous or mixed notifiable waste and you need a movement document for import/export of hazardous or mixed notifiable waste. | C669 & C670                  |

  Scenario: Measure containing multiple options
    Given the below measure type descriptions exist in the database
      | id | measureTypeId | descriptionOverlay                                    | locale | published |
      | 1  | 724           | Import control of fluorinated greenhouse gases edited | EN     | true      |
    And the below document code descriptions exist in the database
      | id | documentCode | descriptionOverlay   | locale | published |
      | 1  | C082         | description for C082 | EN     | true      |
      | 2  | Y951         | description for Y951 | EN     | true      |
      | 3  | C057         | description for C057 | EN     | true      |
      | 4  | C079         | description for C079 | EN     | true      |
      | 5  | Y054         | description for Y054 | EN     | true      |
      | 6  | Y053         | description for Y053 | EN     | true      |
      | 7  | Y926         | description for Y926 | EN     | true      |
    When I call the signposting API for commodity code 8415810091 with additional code 4999 and trade type IMPORT and origin country code CN and user type DECLARING_TRADER and destination country code GB
    Then the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control of fluorinated greenhouse gases edited
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | National Document: CDS universal waiver | 999L                         |
      | EXCEPTION         | description for Y926                    | Y926                         |
    And the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control of fluorinated greenhouse gases edited
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | EXCEPTION         | National Document: CDS universal waiver | 999L                         |
      | EXCEPTION         | description for Y053                    | Y053                         |
      | EXCEPTION         | description for Y054                    | Y054                         |
    And the response includes the measures option data below under section Check if you need an import license or certificate and measure Import control of fluorinated greenhouse gases edited
      | measureOptionType | measureOptionDescriptionOverlay         | measureOptionCertificateCode |
      | CERTIFICATE       | description for C057                    | C057                         |
      | CERTIFICATE       | description for C079                    | C079                         |
      | CERTIFICATE       | description for C082                    | C082                         |
      | EXCEPTION         | National Document: CDS universal waiver | 999L                         |
      | EXCEPTION         | description for Y951                    | Y951                         |
