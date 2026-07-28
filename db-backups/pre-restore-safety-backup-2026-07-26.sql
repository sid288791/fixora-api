--
-- PostgreSQL database dump
--

\restrict cqei5ZnGagxqJJhavyPgjYLC99Z6lFpckUrhDMUfduIsAxNdT8Kgd0unhdugYdD

-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE IF EXISTS ONLY public.audit_events DROP CONSTRAINT IF EXISTS fkr2nbs0h3djuplae34p7aua3b0;
ALTER TABLE IF EXISTS ONLY public.alert_config DROP CONSTRAINT IF EXISTS fkpeee8uo8ogvu4hm1kfjqloesw;
ALTER TABLE IF EXISTS ONLY public.keep_resource_mappings DROP CONSTRAINT IF EXISTS fkfyw1ssi4apdo1o3olla26cl27;
ALTER TABLE IF EXISTS ONLY public.alert_workflow DROP CONSTRAINT IF EXISTS fkc82wjme8x83ga8ptgosi5k7om;
ALTER TABLE IF EXISTS ONLY public.notification_channel DROP CONSTRAINT IF EXISTS fk8i79mtb80tego5ap3guctab3l;
ALTER TABLE IF EXISTS ONLY public.alert_workflow DROP CONSTRAINT IF EXISTS fk6jnah8qpv0rs5lknvjp4327qc;
DROP INDEX IF EXISTS public.idx_users_email;
DROP INDEX IF EXISTS public.idx_applications_status;
DROP INDEX IF EXISTS public.idx_applications_alias;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE IF EXISTS ONLY public.users DROP CONSTRAINT IF EXISTS users_email_key;
ALTER TABLE IF EXISTS ONLY public.notification_channel DROP CONSTRAINT IF EXISTS notification_channel_pkey;
ALTER TABLE IF EXISTS ONLY public.keep_resource_mappings DROP CONSTRAINT IF EXISTS keep_resource_mappings_pkey;
ALTER TABLE IF EXISTS ONLY public.audit_events DROP CONSTRAINT IF EXISTS audit_events_pkey;
ALTER TABLE IF EXISTS ONLY public.applications DROP CONSTRAINT IF EXISTS applications_pkey;
ALTER TABLE IF EXISTS ONLY public.applications DROP CONSTRAINT IF EXISTS applications_alias_key;
ALTER TABLE IF EXISTS ONLY public.alert_workflow DROP CONSTRAINT IF EXISTS alert_workflow_pkey;
ALTER TABLE IF EXISTS ONLY public.alert_config DROP CONSTRAINT IF EXISTS alert_config_pkey;
ALTER TABLE IF EXISTS public.users ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.notification_channel ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.keep_resource_mappings ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.audit_events ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.applications ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.alert_workflow ALTER COLUMN id DROP DEFAULT;
ALTER TABLE IF EXISTS public.alert_config ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE IF EXISTS public.users_id_seq;
DROP TABLE IF EXISTS public.users;
DROP SEQUENCE IF EXISTS public.notification_channel_id_seq;
DROP TABLE IF EXISTS public.notification_channel;
DROP SEQUENCE IF EXISTS public.keep_resource_mappings_id_seq;
DROP TABLE IF EXISTS public.keep_resource_mappings;
DROP SEQUENCE IF EXISTS public.audit_events_id_seq;
DROP TABLE IF EXISTS public.audit_events;
DROP SEQUENCE IF EXISTS public.applications_id_seq;
DROP TABLE IF EXISTS public.applications;
DROP SEQUENCE IF EXISTS public.alert_workflow_id_seq;
DROP TABLE IF EXISTS public.alert_workflow;
DROP SEQUENCE IF EXISTS public.alert_config_id_seq;
DROP TABLE IF EXISTS public.alert_config;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alert_config; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.alert_config (
    id bigint NOT NULL,
    alert_type character varying(255) NOT NULL,
    application_id bigint NOT NULL,
    channels character varying(255),
    condition_expression text,
    created_at timestamp(6) without time zone,
    description character varying(255),
    enabled boolean NOT NULL,
    environment character varying(255),
    goalert_service_url character varying(255),
    name character varying(255) NOT NULL,
    owning_ad_grp character varying(255) NOT NULL,
    service_id bigint,
    severity character varying(255) NOT NULL,
    source character varying(255),
    status character varying(255) NOT NULL,
    teams_webhook_url character varying(255),
    trigger_ai_investigation boolean NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.alert_config OWNER TO fixora_user;

--
-- Name: alert_config_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.alert_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alert_config_id_seq OWNER TO fixora_user;

--
-- Name: alert_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.alert_config_id_seq OWNED BY public.alert_config.id;


--
-- Name: alert_workflow; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.alert_workflow (
    id bigint NOT NULL,
    alert_configuration_id bigint NOT NULL,
    application_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    description character varying(255),
    execution_count bigint NOT NULL,
    is_active boolean NOT NULL,
    keep_workflow_id character varying(255),
    last_triggered_at timestamp(6) without time zone,
    name character varying(255) NOT NULL,
    notification_channel_ids text,
    owning_ad_grp character varying(255) NOT NULL,
    service_id bigint,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.alert_workflow OWNER TO fixora_user;

--
-- Name: alert_workflow_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.alert_workflow_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.alert_workflow_id_seq OWNER TO fixora_user;

--
-- Name: alert_workflow_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.alert_workflow_id_seq OWNED BY public.alert_workflow.id;


--
-- Name: applications; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.applications (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    alias character varying(255) NOT NULL,
    owner_email character varying(255) NOT NULL,
    ad_group_mapping character varying(255) NOT NULL,
    description text,
    status character varying(50) DEFAULT 'ACTIVE'::character varying,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.applications OWNER TO fixora_user;

--
-- Name: applications_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.applications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.applications_id_seq OWNER TO fixora_user;

--
-- Name: applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.applications_id_seq OWNED BY public.applications.id;


--
-- Name: audit_events; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.audit_events (
    id bigint NOT NULL,
    action character varying(255) NOT NULL,
    application_id bigint NOT NULL,
    change_summary text,
    created_at timestamp(6) without time zone,
    entity_id character varying(255) NOT NULL,
    entity_type character varying(255) NOT NULL,
    new_values text,
    old_values text,
    owning_ad_grp character varying(255) NOT NULL,
    performed_by character varying(255) NOT NULL,
    service_id bigint,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.audit_events OWNER TO fixora_user;

--
-- Name: audit_events_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.audit_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.audit_events_id_seq OWNER TO fixora_user;

--
-- Name: audit_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.audit_events_id_seq OWNED BY public.audit_events.id;


--
-- Name: keep_resource_mappings; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.keep_resource_mappings (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    keep_resource_id character varying(255) NOT NULL,
    keep_resource_name character varying(255),
    local_id character varying(255) NOT NULL,
    metadata text,
    owning_ad_grp character varying(255) NOT NULL,
    resource_type character varying(255) NOT NULL,
    service_id bigint,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.keep_resource_mappings OWNER TO fixora_user;

--
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.keep_resource_mappings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.keep_resource_mappings_id_seq OWNER TO fixora_user;

--
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.keep_resource_mappings_id_seq OWNED BY public.keep_resource_mappings.id;


--
-- Name: notification_channel; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.notification_channel (
    id bigint NOT NULL,
    application_id bigint NOT NULL,
    channel_type character varying(255) NOT NULL,
    configuration text,
    created_at timestamp(6) without time zone,
    description character varying(255),
    endpoint character varying(255) NOT NULL,
    is_default boolean NOT NULL,
    name character varying(255) NOT NULL,
    owning_ad_grp character varying(255) NOT NULL,
    service_id bigint,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.notification_channel OWNER TO fixora_user;

--
-- Name: notification_channel_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.notification_channel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.notification_channel_id_seq OWNER TO fixora_user;

--
-- Name: notification_channel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.notification_channel_id_seq OWNED BY public.notification_channel.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.users (
    id bigint NOT NULL,
    name character varying(255) NOT NULL,
    email character varying(255) NOT NULL,
    ad_group character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.users OWNER TO fixora_user;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO fixora_user;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: alert_config id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config ALTER COLUMN id SET DEFAULT nextval('public.alert_config_id_seq'::regclass);


--
-- Name: alert_workflow id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow ALTER COLUMN id SET DEFAULT nextval('public.alert_workflow_id_seq'::regclass);


--
-- Name: applications id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications ALTER COLUMN id SET DEFAULT nextval('public.applications_id_seq'::regclass);


--
-- Name: audit_events id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events ALTER COLUMN id SET DEFAULT nextval('public.audit_events_id_seq'::regclass);


--
-- Name: keep_resource_mappings id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings ALTER COLUMN id SET DEFAULT nextval('public.keep_resource_mappings_id_seq'::regclass);


--
-- Name: notification_channel id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel ALTER COLUMN id SET DEFAULT nextval('public.notification_channel_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Data for Name: alert_config; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.alert_config (id, alert_type, application_id, channels, condition_expression, created_at, description, enabled, environment, goalert_service_url, name, owning_ad_grp, service_id, severity, source, status, teams_webhook_url, trigger_ai_investigation, updated_at) FROM stdin;
\.


--
-- Data for Name: alert_workflow; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.alert_workflow (id, alert_configuration_id, application_id, created_at, description, execution_count, is_active, keep_workflow_id, last_triggered_at, name, notification_channel_ids, owning_ad_grp, service_id, status, updated_at) FROM stdin;
\.


--
-- Data for Name: applications; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.applications (id, name, alias, owner_email, ad_group_mapping, description, status, created_at, updated_at) FROM stdin;
1	Sample App	sample-app	admin@example.com	dev-team,qa-team	Sample application for testing	ACTIVE	2026-07-23 10:02:12.496299	2026-07-23 10:02:12.496299
3	CheckOnly	check-only-app	x@example.com	g	\N	ACTIVE	2026-07-26 17:02:27.294502	2026-07-26 17:02:27.294502
4	ashtest	ashtest	ashtest@gmail.com	ash	test	ACTIVE	2026-07-26 17:18:25.470791	2026-07-26 17:18:25.470791
\.


--
-- Data for Name: audit_events; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.audit_events (id, action, application_id, change_summary, created_at, entity_id, entity_type, new_values, old_values, owning_ad_grp, performed_by, service_id, status, updated_at) FROM stdin;
\.


--
-- Data for Name: keep_resource_mappings; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.keep_resource_mappings (id, application_id, created_at, keep_resource_id, keep_resource_name, local_id, metadata, owning_ad_grp, resource_type, service_id, status, updated_at) FROM stdin;
\.


--
-- Data for Name: notification_channel; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.notification_channel (id, application_id, channel_type, configuration, created_at, description, endpoint, is_default, name, owning_ad_grp, service_id, status, updated_at) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.users (id, name, email, ad_group, created_at, updated_at) FROM stdin;
1	Admin User	admin@example.com	admin-group	2026-07-23 10:02:12.494416	2026-07-23 10:02:12.494416
2	User One	user1@example.com	dev-team	2026-07-23 10:02:12.494416	2026-07-23 10:02:12.494416
\.


--
-- Name: alert_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.alert_config_id_seq', 1, false);


--
-- Name: alert_workflow_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.alert_workflow_id_seq', 1, false);


--
-- Name: applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.applications_id_seq', 4, true);


--
-- Name: audit_events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.audit_events_id_seq', 1, false);


--
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.keep_resource_mappings_id_seq', 1, false);


--
-- Name: notification_channel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.notification_channel_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.users_id_seq', 2, true);


--
-- Name: alert_config alert_config_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config
    ADD CONSTRAINT alert_config_pkey PRIMARY KEY (id);


--
-- Name: alert_workflow alert_workflow_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT alert_workflow_pkey PRIMARY KEY (id);


--
-- Name: applications applications_alias_key; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_alias_key UNIQUE (alias);


--
-- Name: applications applications_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_pkey PRIMARY KEY (id);


--
-- Name: audit_events audit_events_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events
    ADD CONSTRAINT audit_events_pkey PRIMARY KEY (id);


--
-- Name: keep_resource_mappings keep_resource_mappings_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings
    ADD CONSTRAINT keep_resource_mappings_pkey PRIMARY KEY (id);


--
-- Name: notification_channel notification_channel_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel
    ADD CONSTRAINT notification_channel_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: idx_applications_alias; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_applications_alias ON public.applications USING btree (alias);


--
-- Name: idx_applications_status; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_applications_status ON public.applications USING btree (status);


--
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- Name: alert_workflow fk6jnah8qpv0rs5lknvjp4327qc; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT fk6jnah8qpv0rs5lknvjp4327qc FOREIGN KEY (alert_configuration_id) REFERENCES public.alert_config(id);


--
-- Name: notification_channel fk8i79mtb80tego5ap3guctab3l; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel
    ADD CONSTRAINT fk8i79mtb80tego5ap3guctab3l FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- Name: alert_workflow fkc82wjme8x83ga8ptgosi5k7om; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT fkc82wjme8x83ga8ptgosi5k7om FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- Name: keep_resource_mappings fkfyw1ssi4apdo1o3olla26cl27; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings
    ADD CONSTRAINT fkfyw1ssi4apdo1o3olla26cl27 FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- Name: alert_config fkpeee8uo8ogvu4hm1kfjqloesw; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config
    ADD CONSTRAINT fkpeee8uo8ogvu4hm1kfjqloesw FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- Name: audit_events fkr2nbs0h3djuplae34p7aua3b0; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events
    ADD CONSTRAINT fkr2nbs0h3djuplae34p7aua3b0 FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- PostgreSQL database dump complete
--

\unrestrict cqei5ZnGagxqJJhavyPgjYLC99Z6lFpckUrhDMUfduIsAxNdT8Kgd0unhdugYdD

