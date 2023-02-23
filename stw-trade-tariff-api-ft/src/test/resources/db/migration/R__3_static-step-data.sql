
-- Register your business for importing

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (1, 'Get an EORI number', null, 'https://www.gov.uk/eori?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 1,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (1, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (1, 'Check if you need to register for VAT', null, 'https://www.gov.uk/vat-registration/when-to-register?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 2,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (2, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (1, 'Find out about using simplified declaration procedures', null, 'https://www.gov.uk/guidance/using-simplified-declarations-for-imports?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 3,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (3, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (1, 'Check if Authorised Economic Operator status is right for you', null, 'https://www.gov.uk/guidance/authorised-economic-operator-certification?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 4,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (4, 'IMPORT', true);

-- Value your goods and calculate taxes

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (2, 'Work out the value of your goods for customs', null, 'https://www.gov.uk/guidance/how-to-value-your-imports-for-customs-duty-and-trade-statistics?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 5,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (5, 'IMPORT', true);

--- Delay or reduce duty payments

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (3, 'Find out if you can pay a lower rate of duty or delay paying', null, 'https://www.gov.uk/guidance/check-if-you-can-pay-a-reduced-rate-of-customs-duty?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 6,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (6, 'IMPORT', true);

-- Check which transportation documents you need

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (5, 'Find out which documents are needed for your goods to travel', null, 'https://www.gov.uk/guidance/international-trade-paperwork-the-basics'); -- 7,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (7, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (5, 'Read about trade contracts and incoterms', null, 'https://www.great.gov.uk/advice/prepare-for-export-procedures-and-logistics/international-trade-contracts-and-incoterms/'); -- 8,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (8, 'IMPORT', true);

-- Submit declarations and notifications

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'Get UK customs clearance when importing goods from outside the EU: step by step', null, 'https://www.gov.uk/import-customs-declaration'); -- 9,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (9, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'Find out how to hire someone to deal with customs declarations for you', null, 'https://www.gov.uk/guidance/appoint-someone-to-deal-with-customs-on-your-behalf'); -- 10,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (10, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'A pre-arrival notification submitted through Procedure for Electronic Application for Certificates from the horticultural Inspectorate (PEACH)', null, 'http://ehmipeach.defra.gov.uk/Default.aspx?Module=Register'); -- 11,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (11, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'A pre-arrival notification submitted through the Port Health Authority pre-notification system', null, 'http://www.porthealthassociation.co.uk/port-directory/'); -- 12,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (12, 'IMPORT', true);

-- Claim a VAT refund

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (7, 'Find out how to claim a VAT refund', null, 'https://www.gov.uk/guidance/vat-imports-acquisitions-and-purchases-from-abroad?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5#goods-from-non-eu-countries'); -- 13,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (13, 'IMPORT', true);

-- Reclaim duty if you’ve paid the wrong amount

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (8, 'Find out how to apply for a refund on import duties', null, 'https://www.gov.uk/guidance/refunds-and-waivers-on-customs-debt?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5'); -- 14,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (14, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (8, 'Find out what you can claim back if you reject an import', null, 'https://www.gov.uk/guidance/refunds-and-waivers-on-customs-debt?step-by-step-nav=849f71d1-f290-4a8e-9458-add936efefc5#claims-for-rejected-imports'); -- 15,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (15, 'IMPORT', true);

-- Check which invoices and records you should keep

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (9, 'If you imported controlled goods, for example firearms, keep the paperwork that shows who owns the goods.', null, null); -- 16,
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (16, 'IMPORT', true);
