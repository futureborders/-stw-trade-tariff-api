CREATE TYPE locale AS ENUM (
    'EN',
    'CY'
    );

CREATE TYPE assignable_commodity_type AS ENUM (
    'LEAF',
    'SUB_HEADING',
    'HEADING'
    );

CREATE TYPE trade_type AS ENUM (
    'IMPORT',
    'EXPORT'
    );

CREATE TYPE user_type AS ENUM (
    'NON_DECLARING_TRADER',
    'DECLARING_TRADER',
    'INTERMEDIARY'
    );

CREATE TYPE audit_action AS ENUM (
    'ADD_STEP',
    'UPDATE_STEP',
    'DELETE_STEP',
    'ADD_SECTION_ASSIGNMENT',
    'DELETE_SECTION_ASSIGNMENT',
    'ADD_CHAPTER_ASSIGNMENT',
    'DELETE_CHAPTER_ASSIGNMENT',
    'ADD_COMMODITY_ASSIGNMENT',
    'DELETE_COMMODITY_ASSIGNMENT',
    'UPDATE_MEASURE_TYPE_DESCRIPTION',
    'UPDATE_DOCUMENT_CODE_DESCRIPTION',
    'DELETE_DOCUMENT_CODE_DESCRIPTION',
    'DELETE_MEASURE_TYPE_DESCRIPTION',
    'APPROVE_CONTENT',
    'REJECT_CONTENT',
    'DELETE_REJECTED_CONTENT'
    );

CREATE TABLE chapters
(
    chapter_id  SERIAL PRIMARY KEY,
    description character varying NOT NULL
);

CREATE TABLE measure_types
(
    measure_type_id character varying(3) PRIMARY KEY,
    description     character varying NOT NULL
);

CREATE TABLE measure_type_descriptions
(
    id                  SERIAL PRIMARY KEY,
    measure_type_id     character varying(3) NOT NULL,
    locale              locale               NOT NULL,
    description_overlay character varying    NOT NULL,
    subtext             character varying    NULL,
    published           boolean              NOT NULL DEFAULT true,
    CONSTRAINT measure_type_descriptions_un UNIQUE ( locale)
);

CREATE TABLE sections
(
    section_id  SERIAL PRIMARY KEY,
    description character varying NOT NULL
);

CREATE TABLE signposting_step_chapter_assignment
(
    id                  SERIAL PRIMARY KEY,
    signposting_step_id integer NOT NULL,
    chapter_id          integer NOT NULL,
    published           boolean NOT NULL DEFAULT true
);

CREATE TABLE signposting_step_commodity_assignment
(
    id                  SERIAL PRIMARY KEY,
    signposting_step_id integer                   NOT NULL,
    code                varchar(10)               NOT NULL,
    commodity_type      assignable_commodity_type NOT NULL,
    published           boolean                   NOT NULL DEFAULT true
);

CREATE TABLE signposting_step_section_assignment
(
    id                  SERIAL PRIMARY KEY,
    signposting_step_id integer NOT NULL,
    section_id          integer NOT NULL,
    published           boolean NOT NULL DEFAULT true
);

CREATE TABLE signposting_step_trade_type_assignment
(
    id                  SERIAL PRIMARY KEY,
    signposting_step_id integer    NOT NULL,
    trade_type          trade_type NOT NULL,
    blanket_apply       boolean    NOT NULL
);

CREATE TABLE signposting_super_headers (
 id           SERIAL  NOT NULL,
 order_index  int4    NOT NULL,
 CONSTRAINT signposting_super_headers_pkey PRIMARY KEY (id)
 );

CREATE TABLE signposting_step_headers
(
    id              SERIAL     NOT NULL,
    order_index     int        NOT NULL,
    trade_type      trade_type NOT NULL,
    super_header_id int4       NOT NULL,
    CONSTRAINT signposting_steps_headers_pkey PRIMARY KEY (id),
    CONSTRAINT signposting_steps_headers_super_headers_id_fkey FOREIGN KEY (super_header_id) REFERENCES signposting_super_headers(id) ON DELETE CASCADE
);

CREATE TABLE signposting_super_header_descriptions (
 id                             SERIAL     NOT NULL,
 super_header_id                int4       NOT NULL,
 locale                         locale     NOT NULL,
 user_type                      user_type  NOT NULL,
 super_header_description       text       NOT NULL,
 super_header_explanatory_text  text,
 CONSTRAINT signposting_super_header_descriptions_pkey PRIMARY KEY (id),
 CONSTRAINT signposting_super_header_descriptions_un UNIQUE (super_header_id, locale, user_type),
 CONSTRAINT signposting_super_header_descriptions_super_header_id_fkey FOREIGN KEY (super_header_id) REFERENCES signposting_super_headers(id) ON DELETE CASCADE
 );

CREATE TABLE signposting_step_header_descriptions
(
    id                      SERIAL    NOT NULL,
    header_id               int4      NOT NULL,
    user_type               user_type NOT NULL,
    locale                  locale    NOT NULL,
    header_description      text      NOT NULL,
    header_explanatory_text text      NULL,
    header_link_text        text      NOT NULL,
    external_link           text      NULL,
    CONSTRAINT signposting_steps_headers_description_pkey PRIMARY KEY (id)
);

CREATE TABLE signposting_step_subheaders
(
    id          SERIAL     NOT NULL,
    order_index int        NOT NULL,
    trade_type  trade_type NOT NULL,
    CONSTRAINT signposting_steps_subheaders_pkey PRIMARY KEY (id)
);

CREATE TABLE signposting_step_subheader_descriptions
(
    id                    SERIAL    NOT NULL,
    header_id             int4      NOT NULL,
    user_type             user_type NOT NULL,
    locale                locale    NOT NULL,
    subheader_description text      NOT NULL,
    CONSTRAINT signposting_steps_subheaders_description_pkey PRIMARY KEY (id)
);

CREATE TABLE signposting_steps
(
    id                     SERIAL NOT NULL,
    step_description       text   NOT NULL,
    step_howto_description text   NULL,
    step_url               text   NULL,
    published              boolean NOT NULL DEFAULT true,
    CONSTRAINT signposting_steps_pkey PRIMARY KEY (id)
);

CREATE TABLE public.signposting_step_heading_assignment
(
    id                  serial     NOT NULL,
    signposting_step_id int4       NOT NULL,
    trade_type          trade_type NOT NULL,
    header_id           int4       NOT NULL,
    sub_header_id       int4       NOT NULL,
    CONSTRAINT signposting_step_heading_assignment_un UNIQUE (signposting_step_id, trade_type)
);

CREATE TABLE document_codes
(
    document_code varchar(4) NOT NULL,
    description   varchar    NOT NULL,
    CONSTRAINT document_codes_pk PRIMARY KEY (document_code)
);

CREATE TABLE document_code_descriptions
(
    id                  serial            NOT NULL,
    document_code       varchar(4)        NOT NULL,
    locale              locale            NOT NULL,
    description_overlay character varying NOT NULL,
    subtext             character varying NULL,
    url                 character varying NULL,
    url_text            character varying NULL,
    published           boolean           NOT NULL DEFAULT true,
    CONSTRAINT document_code_descriptions_pk PRIMARY KEY (id),
    CONSTRAINT document_code_descriptions_un UNIQUE (document_code, locale)
);

CREATE TABLE entity_audit
(
    id                  SERIAL                        NOT NULL,
    entity_id integer                       NOT NULL,
    audit_action        audit_action NOT NULL,
    data                jsonb                         NOT NULL,
    created_by          character varying,
    created_at          timestamptz                   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT entity_audit_pkey PRIMARY KEY (id)
);

ALTER TABLE ONLY document_code_descriptions
    ADD CONSTRAINT document_code_descriptions_fk FOREIGN KEY (document_code) REFERENCES document_codes (document_code) ON DELETE CASCADE;

ALTER TABLE ONLY measure_type_descriptions
    ADD CONSTRAINT measure_type_descriptions_fk FOREIGN KEY (measure_type_id) REFERENCES measure_types (measure_type_id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_heading_assignment
    ADD CONSTRAINT signposting_step_heading_assignment_fk FOREIGN KEY (signposting_step_id) REFERENCES signposting_steps (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_heading_assignment
    ADD CONSTRAINT signposting_step_heading_assignment_fk_1 FOREIGN KEY (header_id) REFERENCES signposting_step_headers (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_header_descriptions
    ADD CONSTRAINT signposting_step_header_descriptions_fk_1 FOREIGN KEY (header_id) REFERENCES signposting_step_headers (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_subheader_descriptions
    ADD CONSTRAINT signposting_step_subheader_descriptions_fk_1 FOREIGN KEY (header_id) REFERENCES signposting_step_subheaders (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_heading_assignment
    ADD CONSTRAINT signposting_step_heading_assignment_fk_2 FOREIGN KEY (sub_header_id) REFERENCES signposting_step_subheaders (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_chapter_assignment
    ADD CONSTRAINT signposting_step_chapter_assignment_chapter_id_fkey FOREIGN KEY (chapter_id) REFERENCES chapters (chapter_id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_chapter_assignment
    ADD CONSTRAINT signposting_step_chapter_assignment_signposting_step_id_fkey FOREIGN KEY (signposting_step_id) REFERENCES signposting_steps (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_commodity_assignment
    ADD CONSTRAINT signposting_step_commodity_assignment_signposting_step_id_fkey FOREIGN KEY (signposting_step_id) REFERENCES signposting_steps (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_section_assignment
    ADD CONSTRAINT signposting_step_section_assignment_section_id_fkey FOREIGN KEY (section_id) REFERENCES sections (section_id);

ALTER TABLE ONLY signposting_step_section_assignment
    ADD CONSTRAINT signposting_step_section_assignment_signposting_step_id_fkey FOREIGN KEY (signposting_step_id) REFERENCES signposting_steps (id) ON DELETE CASCADE;

ALTER TABLE ONLY signposting_step_trade_type_assignment
    ADD CONSTRAINT signposting_step_trade_type_assignment_id_fkey FOREIGN KEY (signposting_step_id) REFERENCES signposting_steps (id) ON DELETE CASCADE;

ALTER TABLE entity_audit ALTER COLUMN audit_action TYPE VARCHAR(255);

ALTER TABLE entity_audit ALTER COLUMN audit_action TYPE audit_action USING (audit_action::audit_action);

CREATE TYPE content_modification_action_type AS ENUM (
    'STEP_ADDITION',
    'STEP_UPDATE',
    'STEP_DELETE',
    'ADD_SECTION_ASSIGNMENT',
    'DELETE_SECTION_ASSIGNMENT',
    'ADD_CHAPTER_ASSIGNMENT',
    'DELETE_CHAPTER_ASSIGNMENT',
    'ADD_COMMODITY_ASSIGNMENT',
    'DELETE_COMMODITY_ASSIGNMENT',
    'DELETE_DOCUMENT_CODE_DESCRIPTION',
    'MEASURE_TYPE_DESCRIPTIONS_DELETION',
    'MEASURE_TYPE_UPDATE',
    'DOCUMENT_CODE_UPDATE');

ALTER TABLE public.document_code_descriptions
    DROP CONSTRAINT document_code_descriptions_un;

ALTER TABLE public.measure_type_descriptions
    DROP CONSTRAINT measure_type_descriptions_un;

ALTER TABLE public.signposting_steps
    ADD destination_country_restrictions text[] NOT NULL DEFAULT '{GB,XI}',
    ADD origin_country_restrictions      text[] NOT NULL DEFAULT '{ALL}';

ALTER TABLE public.measure_type_descriptions
    ADD destination_country_restrictions text[] NOT NULL DEFAULT '{GB,XI}';

ALTER TABLE public.document_code_descriptions
    ADD destination_country_restrictions text[] NOT NULL DEFAULT '{GB,XI}';

create table prohibition_descriptions (
	id SERIAL NOT NULL,
	legal_act  character varying(8) NOT NULL,
	origin_country character varying(2),
	description character varying NOT NULL,
	locale locale NOT NULL,
	published bool,
	CONSTRAINT prohibition_descriptions_ukey UNIQUE (legal_act, origin_country,locale,published),
	CONSTRAINT prohibition_descriptions_pkey PRIMARY KEY (id)
);


insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('A1900160', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('A1907820', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('A1907950', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('C2100230', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('C2100260', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1904110', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1904110', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1905830', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1905830', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1907420', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1907420', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X1907920', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X2006420', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X2007070', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);
insert into prohibition_descriptions ( legal_act, origin_country, description, locale, published) values ('X2013540', null, 'Prohibitions and restrictions enforced by customs on goods', 'EN', true);


ALTER TABLE signposting_step_heading_assignment ALTER COLUMN sub_header_id DROP NOT NULL;
ALTER TABLE signposting_steps ALTER COLUMN step_description DROP NOT NULL;


ALTER TABLE signposting_steps
  ADD COLUMN non_declaring_trader_content text,
  ADD COLUMN declaring_trader_content text,
  ADD COLUMN agent_content text,
  ADD COLUMN order_index int NOT NULL DEFAULT 1;

ALTER TABLE signposting_steps ADD header_id int4;

ALTER TABLE signposting_steps ADD FOREIGN KEY (header_id) REFERENCES signposting_step_headers(id);

ALTER TABLE signposting_steps ALTER COLUMN header_id SET NOT NULL;

DROP TABLE signposting_step_subheaders, signposting_step_subheader_descriptions, signposting_step_heading_assignment;

ALTER TABLE signposting_step_headers DROP COLUMN trade_type;

ALTER TABLE public.signposting_step_headers ADD related_entity_type text DEFAULT NULL;
