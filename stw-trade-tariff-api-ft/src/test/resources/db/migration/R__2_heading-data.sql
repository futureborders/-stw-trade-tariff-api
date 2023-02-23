INSERT INTO signposting_super_headers (order_index) VALUES (1);
INSERT INTO signposting_super_headers (order_index) VALUES (2);
INSERT INTO signposting_super_headers (order_index) VALUES (3);

INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (1, 'EN', 'NON_DECLARING_TRADER', 'Before you buy the goods', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (2, 'EN', 'NON_DECLARING_TRADER', 'Prepare and submit customs documentation', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (3, 'EN', 'NON_DECLARING_TRADER', 'After your goods arrive in the UK', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (1, 'EN', 'DECLARING_TRADER', 'Before you buy the goods', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (2, 'EN', 'DECLARING_TRADER', 'Prepare and submit customs documentation', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (3, 'EN', 'DECLARING_TRADER', 'After your goods arrive in the UK', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (1, 'EN', 'INTERMEDIARY', 'Before your importer buys the goods', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (2, 'EN', 'INTERMEDIARY', 'Prepare and submit customs documentation', NULL);
INSERT INTO signposting_super_header_descriptions (super_header_id, locale, user_type, super_header_description, super_header_explanatory_text) VALUES (3, 'EN', 'INTERMEDIARY', 'After the goods arrive in the UK', NULL);

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (1, 1, 1);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (1, 'NON_DECLARING_TRADER', 'EN', 'Register your business for importing', NULL,'Register to bring goods across border');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (1, 'DECLARING_TRADER', 'EN', 'Register your business for importing', NULL,'Register to bring goods across border');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (1, 'INTERMEDIARY', 'EN', 'Get business information from your customer', NULL,'Register to bring goods across border');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (2, 2, 1);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (2, 'NON_DECLARING_TRADER', 'EN', 'Value your goods', NULL,'Calculate tax and duty');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (2, 'DECLARING_TRADER', 'EN', 'Value your goods', NULL,'Calculate tax and duty');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (2, 'INTERMEDIARY', 'EN', 'Check value of goods', NULL,'Calculate tax and duty');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (3, 3, 1);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (3, 'NON_DECLARING_TRADER', 'EN', 'Delay or reduce duty payments', NULL,'Delay duty payments');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (3, 'DECLARING_TRADER', 'EN', 'Delay or reduce duty payments', NULL,'Delay duty payments');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (3, 'INTERMEDIARY', 'EN', 'Delay or reduce duty payments for your customer', NULL,'Delay duty payments');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (4, 4, 1);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (4, 'NON_DECLARING_TRADER', 'EN', 'Check if you need an import license or certificate', 'Certificates can help reduce the cost of duties and make distributing your goods much easier.<br/><br/>All certificates should be obtained before your goods reach the UK border.','Licences certificates restrictions');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (4, 'DECLARING_TRADER', 'EN', 'Check if you need an import license or certificate', 'Certificates can help reduce the cost of duties and make distributing your goods much easier.<br/><br/>All certificates should be obtained before your goods reach the UK border.','Licences certificates restrictions');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (4, 'INTERMEDIARY', 'EN', 'Check if an import license or certificate is needed', 'Certificates can help reduce the cost of duties and make distributing your goods much easier.<br/><br/>All certificates should be obtained before your goods reach the UK border.','Licences certificates restrictions');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (5, 5, 2);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (5, 'NON_DECLARING_TRADER', 'EN', 'Check which transportation documents you need', NULL,'Check information documents');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (5, 'DECLARING_TRADER', 'EN', 'Check which transportation documents you need', NULL,'Check information documents');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (5, 'INTERMEDIARY', 'EN', 'Check which transportation documents are needed', NULL,'Check information documents');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (6, 6, 2);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text, external_link) VALUES (6, 'DECLARING_TRADER', 'EN', 'Submit declarations and notifications', 'You need to register to use each of these services if you have not submitted a notification before.<br/><br/>Notifications inform the right government departments that your goods are due to arrive. They are needed to get your goods through the UK border.','Submit declarations notifications', 'https://externalLink');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text, external_link) VALUES (6, 'INTERMEDIARY', 'EN', 'Submit declarations and notifications', 'You need to register to use each of these services if you have not submitted a notification before.<br/><br/>Notifications inform the right government departments that your goods are due to arrive. They are needed to get your goods through the UK border.','Submit declarations notifications', 'https://externalLink');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (7, 7, 3);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (7, 'NON_DECLARING_TRADER', 'EN', 'Claim a VAT refund', NULL,'Claim back vat customs duties');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (7, 'DECLARING_TRADER', 'EN', 'Claim a VAT refund', NULL,'Claim back vat customs duties');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (7, 'INTERMEDIARY', 'EN', 'Claim a VAT refund', NULL,'Claim back vat customs duties');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (8, 8, 3);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (8, 'NON_DECLARING_TRADER', 'EN', 'Reclaim duty if you''ve paid the wrong amount', NULL,'Reclaim duty');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (8, 'DECLARING_TRADER', 'EN', 'Reclaim duty if you''ve paid the wrong amount', NULL,'Reclaim duty');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (8, 'INTERMEDIARY', 'EN', 'Reclaim duty if your customer has paid the wrong amount', NULL,'Reclaim duty');

INSERT INTO public.signposting_step_headers(id, order_index, super_header_id) VALUES (9, 9, 3);
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (9, 'NON_DECLARING_TRADER', 'EN', 'Check which invoices and records you should keep', NULL, 'Keep invoices paperwork');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (9, 'DECLARING_TRADER', 'EN', 'Check which invoices and records you should keep', NULL, 'Keep invoices paperwork');
INSERT INTO public.signposting_step_header_descriptions(header_id, user_type, locale, header_description, header_explanatory_text, header_link_text) VALUES (9, 'INTERMEDIARY', 'EN', 'Check which invoices and records your customer should keep', NULL,'Keep invoices paperwork');

UPDATE public.signposting_step_headers SET related_entity_type='IMPORT_CONTROLS' WHERE id=4;
UPDATE public.signposting_step_headers SET related_entity_type='CALCULATE_DUTY' WHERE id=2;
UPDATE public.signposting_step_headers SET related_entity_type='IMPORT_REGISTRATION' WHERE id=1;

UPDATE public.signposting_step_headers SET related_entity_type='IMPORT_DOCUMENTATION' WHERE id=5;
UPDATE public.signposting_step_headers SET related_entity_type='IMPORT_DECLARATION' WHERE id=6;

UPDATE public.signposting_step_headers SET related_entity_type='CLAIMING_BACK_DUTY' WHERE id=7;
UPDATE public.signposting_step_headers SET related_entity_type='DELAY_DUTY' WHERE id=3;
UPDATE public.signposting_step_headers SET related_entity_type='IMPORT_RECORD_KEEPING' WHERE id=9;
