
-- Submit declarations and notifications

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'An Entry Summary Declaration (ENS) submitted through the Import Control System (ICS).', null, 'https://www.gov.uk/guidance/register-to-make-an-entry-summary-declaration'); -- 20
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (17, 'IMPORT', true);

INSERT INTO public.signposting_steps(header_id, step_description, step_howto_description, step_url) VALUES (6, 'A Customs Declaration through Customs Handling of Import and Export Freight system (CHIEF)', null, 'https://www.gov.uk/guidance/customs-declarations-for-goods-brought-into-the-eu?step-by-step-nav=8a543f4b-afb7-4591-bbfc-2eec52ab96c2'); -- 22
INSERT INTO public.signposting_step_trade_type_assignment(signposting_step_id, trade_type, blanket_apply) VALUES (18, 'IMPORT', true);




UPDATE public.signposting_steps
SET non_declaring_trader_content='Customs procedure code (CPC)', declaring_trader_content='Customs procedure code (CPC) declaration', agent_content='Agent customs procedure code (CPC) declaration', order_index=1
WHERE id=1;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Declaration unique consignment reference (DUCR)', declaring_trader_content='Declaration unique consignment reference (DUCR) declaration', agent_content='Agent Declaration unique consignment reference (DUCR) declaration', order_index=1
WHERE id=2;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Purchase and, if available, sales invoice numbers', declaring_trader_content='Purchase and, if available, sales invoice numbers declaration', agent_content='Agent   Purchase and, if available, sales invoice numbers declaration', order_index=1
WHERE id=3;


UPDATE public.signposting_steps
SET non_declaring_trader_content='Date and time of entry in records – creating the tax point, which is used for working out VAT payments later', declaring_trader_content='Date and time of entry in records – creating the tax point, which is used for working out VAT payments later declaration', agent_content='Agent Date and time of entry in records – creating the tax point, which is used for working out VAT payments later declaration', order_index=1
WHERE id=4;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Any temporary admission, warehousing or temporary storage stock account references', declaring_trader_content='Any temporary admission, warehousing or temporary storage stock account references declaration', agent_content='Agent Any temporary admission, warehousing or temporary storage stock account references declaration', order_index=1
WHERE id=5;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Warehouse approval number', declaring_trader_content='Warehouse approval number declaration', agent_content='Agent Warehouse approval number declaration', order_index=1
WHERE id=6;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Written description of the goods – so they are easy to identify and to decide the correct commodity code to use
    Customs value', declaring_trader_content='Written description of the goods – so they are easy to identify and to decide the correct commodity code to use
    Customs value declaration', agent_content='Agent Written description of the goods – so they are easy to identify and to decide the correct commodity code to use
    Customs value declaration', order_index=1
WHERE id=7;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Quantity of goods – for example, number of packages and items, net mass', declaring_trader_content='Quantity of goods – for example, number of packages and items, net mass declaration', agent_content='Agent Quantity of goods – for example, number of packages and items, net mass declaration', order_index=1
WHERE id=8;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Details of any licensing requirements and licence numbers', declaring_trader_content='Details of any licensing requirements and licence numbers declaration', agent_content='Agent Details of any licensing requirements and licence numbers declaration', order_index=1
WHERE id=9;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Details of any licensing requirements and licence numbers', declaring_trader_content='Details of any licensing requirements and licence numbers declaration', agent_content='Agent Details of any licensing requirements and licence numbers declaration', order_index=1
WHERE id=9;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Details of any supporting documents, including the serial numbers, where appropriate', declaring_trader_content='Details of any supporting documents, including the serial numbers, where appropriate declaration', agent_content='Agent Details of any supporting documents, including the serial numbers, where appropriate declaration', order_index=1
WHERE id=10;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Details of any supporting documents, including the serial numbers, where appropriate
    (If an agent making a declaration on behalf of someone else) details of the person being represented', declaring_trader_content='Details of any supporting documents, including the serial numbers, where appropriate
    (If an agent making a declaration on behalf of someone else) details of the person being represented declaration', agent_content='Agent Details of any supporting documents, including the serial numbers, where appropriate
    (If an agent making a declaration on behalf of someone else) details of the person being represented declaration', order_index=1
WHERE id=11;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Consignee Identification Number', declaring_trader_content='Consignee Identification Number declaration', agent_content='Agent Consignee Identification Number declaration', order_index=1
WHERE id=12;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Name and Address', declaring_trader_content='Name and Address declaration', agent_content='Agent Name and Address declaration', order_index=1
WHERE id=13;



UPDATE public.signposting_steps
SET non_declaring_trader_content='Declarant Name and Address', declaring_trader_content='Declarant Name and Address declaration', agent_content='Agent Declarant Name and Address declaration', order_index=1
WHERE id=14;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Declarant Identification Number', declaring_trader_content='Declarant Identification Number declaration', agent_content='Declarant Identification Number declaration', order_index=1
WHERE id=15;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Representative Name and Address ', declaring_trader_content='Representative Name and Address  declaration', agent_content='Agent Representative Name and Address  declaration', order_index=1
WHERE id=16;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Representative Identification Number', declaring_trader_content='Representative Identification Number declaration', agent_content='Agent Representative Identification Number declaration', order_index=1
WHERE id=17;

UPDATE public.signposting_steps
SET non_declaring_trader_content='Representative Status Code', declaring_trader_content='Representative Status Code declaration', agent_content='Agent Representative Status Code declaration', order_index=1
WHERE id=18;
