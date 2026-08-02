--
-- PostgreSQL database dump
--

\restrict BxoDVPqGlWMO3Caqbrcmchd8cQX04HGL9pob4K9CmnnlPiQVCCFjUeN20yD0Q5E

-- Dumped from database version 15.18
-- Dumped by pg_dump version 18.4

-- Started on 2026-07-24 21:54:50 IST

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 219 (class 1259 OID 17328)
-- Name: alert_config; Type: TABLE; Schema: public; Owner: fixora_user
--

CREATE TABLE public.alert_config (
    id bigint NOT NULL,
    alert_type character varying(255) NOT NULL,
    application_id bigint NOT NULL,
    condition_expression text,
    created_at timestamp(6) without time zone,
    description character varying(255),
    enabled boolean NOT NULL,
    name character varying(255) NOT NULL,
    owning_ad_grp character varying(255) NOT NULL,
    service_id bigint,
    severity character varying(255) NOT NULL,
    status character varying(255) NOT NULL,
    updated_at timestamp(6) without time zone,
    channels character varying(255),
    environment character varying(255),
    goalert_service_url character varying(255),
    source character varying(255),
    teams_webhook_url character varying(255),
    trigger_ai_investigation boolean DEFAULT false NOT NULL
);


ALTER TABLE public.alert_config OWNER TO fixora_user;

--
-- TOC entry 218 (class 1259 OID 17327)
-- Name: alert_config_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.alert_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alert_config_id_seq OWNER TO fixora_user;

--
-- TOC entry 3528 (class 0 OID 0)
-- Dependencies: 218
-- Name: alert_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.alert_config_id_seq OWNED BY public.alert_config.id;


--
-- TOC entry 221 (class 1259 OID 17337)
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
-- TOC entry 220 (class 1259 OID 17336)
-- Name: alert_workflow_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.alert_workflow_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.alert_workflow_id_seq OWNER TO fixora_user;

--
-- TOC entry 3529 (class 0 OID 0)
-- Dependencies: 220
-- Name: alert_workflow_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.alert_workflow_id_seq OWNED BY public.alert_workflow.id;


--
-- TOC entry 217 (class 1259 OID 16401)
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
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    integration_id character varying
);


ALTER TABLE public.applications OWNER TO fixora_user;

--
-- TOC entry 216 (class 1259 OID 16400)
-- Name: applications_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.applications_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.applications_id_seq OWNER TO fixora_user;

--
-- TOC entry 3530 (class 0 OID 0)
-- Dependencies: 216
-- Name: applications_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.applications_id_seq OWNED BY public.applications.id;


--
-- TOC entry 223 (class 1259 OID 17358)
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
-- TOC entry 222 (class 1259 OID 17357)
-- Name: audit_events_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.audit_events_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_events_id_seq OWNER TO fixora_user;

--
-- TOC entry 3531 (class 0 OID 0)
-- Dependencies: 222
-- Name: audit_events_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.audit_events_id_seq OWNED BY public.audit_events.id;


--
-- TOC entry 225 (class 1259 OID 17367)
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
-- TOC entry 224 (class 1259 OID 17366)
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.keep_resource_mappings_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.keep_resource_mappings_id_seq OWNER TO fixora_user;

--
-- TOC entry 3532 (class 0 OID 0)
-- Dependencies: 224
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.keep_resource_mappings_id_seq OWNED BY public.keep_resource_mappings.id;


--
-- TOC entry 227 (class 1259 OID 17376)
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
    updated_at timestamp(6) without time zone,
    keep_provider_id character varying(255)
);


ALTER TABLE public.notification_channel OWNER TO fixora_user;

--
-- TOC entry 226 (class 1259 OID 17375)
-- Name: notification_channel_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.notification_channel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notification_channel_id_seq OWNER TO fixora_user;

--
-- TOC entry 3533 (class 0 OID 0)
-- Dependencies: 226
-- Name: notification_channel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.notification_channel_id_seq OWNED BY public.notification_channel.id;


--
-- TOC entry 215 (class 1259 OID 16388)
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
-- TOC entry 214 (class 1259 OID 16387)
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: fixora_user
--

CREATE SEQUENCE public.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO fixora_user;

--
-- TOC entry 3534 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fixora_user
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- TOC entry 3334 (class 2604 OID 17331)
-- Name: alert_config id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config ALTER COLUMN id SET DEFAULT nextval('public.alert_config_id_seq'::regclass);


--
-- TOC entry 3336 (class 2604 OID 17340)
-- Name: alert_workflow id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow ALTER COLUMN id SET DEFAULT nextval('public.alert_workflow_id_seq'::regclass);


--
-- TOC entry 3330 (class 2604 OID 17345)
-- Name: applications id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications ALTER COLUMN id SET DEFAULT nextval('public.applications_id_seq'::regclass);


--
-- TOC entry 3337 (class 2604 OID 17361)
-- Name: audit_events id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events ALTER COLUMN id SET DEFAULT nextval('public.audit_events_id_seq'::regclass);


--
-- TOC entry 3338 (class 2604 OID 17370)
-- Name: keep_resource_mappings id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings ALTER COLUMN id SET DEFAULT nextval('public.keep_resource_mappings_id_seq'::regclass);


--
-- TOC entry 3339 (class 2604 OID 17379)
-- Name: notification_channel id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel ALTER COLUMN id SET DEFAULT nextval('public.notification_channel_id_seq'::regclass);


--
-- TOC entry 3327 (class 2604 OID 17384)
-- Name: users id; Type: DEFAULT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- TOC entry 3514 (class 0 OID 17328)
-- Dependencies: 219
-- Data for Name: alert_config; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.alert_config (id, alert_type, application_id, condition_expression, created_at, description, enabled, name, owning_ad_grp, service_id, severity, status, updated_at, channels, environment, goalert_service_url, source, teams_webhook_url, trigger_ai_investigation) FROM stdin;
1	METRIC	1	cpu_usage > 95	2026-07-21 21:14:21.832016	Alert when CPU usage exceeds threshold (updated)	t	High CPU Alert - Updated	dev-team	1	CRITICAL	ACTIVE	2026-07-21 21:14:29.379387	\N	\N	\N	\N	\N	f
2	METRIC	1	mem_usage > 80	2026-07-21 21:17:51.527271	Alert on high memory	t	Memory Alert	qa-team	2	MEDIUM	ACTIVE	2026-07-21 21:17:51.527283	\N	\N	\N	\N	\N	f
13	EVENT	3	time>30min	2026-07-23 18:18:14.041338	Stuck file alert	t	Stuck Alert	aap-team	123	LOW	ACTIVE	2026-07-23 18:18:14.04134	TEAMS,EMAIL,GOALERT	staging		system		t
14	THRESHOLD	5	cpu_usage > 80	2026-07-24 21:44:17.047443	Triggers when CPU usage exceeds 80% for 5 minutes	t	High CPU Usage Alert	test-team	\N	WARNING	ACTIVE	2026-07-24 21:44:17.047445	slack,pagerduty	production	\N	prometheus	\N	f
15	THRESHOLD	7	cpu>80	2026-07-24 21:47:30.246386	CPU alert	t	High CPU Alert	test	\N	WARNING	ACTIVE	2026-07-24 21:47:30.246393	slack	prod	\N	prometheus	\N	f
\.


--
-- TOC entry 3516 (class 0 OID 17337)
-- Dependencies: 221
-- Data for Name: alert_workflow; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.alert_workflow (id, alert_configuration_id, application_id, created_at, description, execution_count, is_active, keep_workflow_id, last_triggered_at, name, notification_channel_ids, owning_ad_grp, service_id, status, updated_at) FROM stdin;
5	15	7	2026-07-24 21:47:30.304743	Workflow for alert: High CPU Alert	0	t	b30087c0-b692-4a37-9f23-e5adb34e6cae	\N	High CPU Alert Workflow	slack	test	\N	ACTIVE	2026-07-24 21:47:30.304746
\.


--
-- TOC entry 3512 (class 0 OID 16401)
-- Dependencies: 217
-- Data for Name: applications; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.applications (id, name, alias, owner_email, ad_group_mapping, description, status, created_at, updated_at, integration_id) FROM stdin;
1	Sample App	sample-app	admin@example.com	dev-team,qa-team	Sample application for testing	ACTIVE	2026-07-21 15:42:36.685744	2026-07-21 15:42:36.685744	\N
2	Payments Service	payments-svc	payments-owner@example.com	payments-team,finance-team	Handles payment processing	ACTIVE	2026-07-21 21:16:05.630976	2026-07-21 21:16:05.630984	\N
3	Advance service 	advance-service	sid@gmail.com	aap-team		ACTIVE	2026-07-23 18:14:46.198018	2026-07-23 18:14:46.198028	f85082a3-776d-4260-8744-4ebc90af9c59
4	Test App	test	test@test.com	test	test	ACTIVE	2026-07-24 21:43:30.766443	2026-07-24 21:43:30.766458	043abb80-08d2-433e-ab4f-1a29e19e284c
5	Test App for Alert Workflow	test-alert-workflow	test@test.com	test-team	Test application for alert workflow creation	ACTIVE	2026-07-24 21:44:17.002923	2026-07-24 21:44:17.002928	5db5915a-26c0-4ee1-9575-9dee9c1f4194
7	Test App2	test2	test@test.com	test	test	ACTIVE	2026-07-24 21:47:27.01016	2026-07-24 21:47:27.010167	7615b6e1-63b8-4a83-9f50-095e5415a562
\.


--
-- TOC entry 3518 (class 0 OID 17358)
-- Dependencies: 223
-- Data for Name: audit_events; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.audit_events (id, action, application_id, change_summary, created_at, entity_id, entity_type, new_values, old_values, owning_ad_grp, performed_by, service_id, status, updated_at) FROM stdin;
1	CREATE	1	CREATE ALERT_CONFIG for 3	2026-07-21 21:19:28.044327	3	ALERT_CONFIG	{"id":3,"applicationId":1,"serviceId":3,"owningAdGrp":"sre-team","name":"Disk Space Alert","description":"Alert on low disk space","alertType":"METRIC","severity":"HIGH","conditionExpression":"disk_free \\u003c 10","enabled":true,"status":"ACTIVE","createdAt":"2026-07-21T21:19:28.031752","updatedAt":"2026-07-21T21:19:28.031758"}	\N	sre-team	siddharth	\N	ACTIVE	2026-07-21 21:19:28.044332
2	DELETE	1	DELETE ALERT_CONFIG for 3	2026-07-21 21:19:34.170885	3	ALERT_CONFIG	\N	{"id":3,"applicationId":1,"serviceId":3,"owningAdGrp":"sre-team","name":"Disk Space Alert","description":"Alert on low disk space","alertType":"METRIC","severity":"HIGH","conditionExpression":"disk_free \\u003c 10","enabled":true,"status":"ACTIVE","createdAt":"2026-07-21T21:19:28.031752","updatedAt":"2026-07-21T21:19:28.031758"}	sre-team	siddharth	3	ACTIVE	2026-07-21 21:19:34.170887
3	CREATE	1	CREATE NOTIFICATION_CHANNEL for 2	2026-07-21 21:19:34.187827	2	NOTIFICATION_CHANNEL	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"Temp Channel","channelType":"EMAIL","endpoint":"test@example.com","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-21T21:19:34.185659","updatedAt":"2026-07-21T21:19:34.185661"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-21 21:19:34.187829
4	DELETE	1	DELETE NOTIFICATION_CHANNEL for 2	2026-07-21 21:19:34.2018	2	NOTIFICATION_CHANNEL	\N	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"Temp Channel","channelType":"EMAIL","endpoint":"test@example.com","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-21T21:19:34.185659","updatedAt":"2026-07-21T21:19:34.185661"}	dev-team	siddharth	1	ACTIVE	2026-07-21 21:19:34.201801
5	DELETE	1	DELETE ALERT_WORKFLOW for 1	2026-07-21 21:19:34.217104	1	ALERT_WORKFLOW	\N	{"id":1,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"CPU Alert Workflow - v2","description":"Workflow triggered on high CPU (v2)","alertConfigurationId":1,"notificationChannelIds":"1","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-21T21:14:39.734649","updatedAt":"2026-07-21T21:14:44.923399"}	dev-team	siddharth	1	ACTIVE	2026-07-21 21:19:34.217107
6	CREATE	1	CREATE ALERT_CONFIG for 4	2026-07-22 21:37:21.196096	4	ALERT_CONFIG	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736441 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:37:21.182046","updatedAt":"2026-07-22T21:37:21.182051"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:37:21.196101
8	DELETE	1	DELETE ALERT_CONFIG for 4	2026-07-22 21:37:21.263666	4	ALERT_CONFIG	\N	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736441 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:37:21.182046","updatedAt":"2026-07-22T21:37:21.182051"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:37:21.263668
9	CREATE	1	CREATE ALERT_CONFIG for 5	2026-07-22 21:41:57.104758	5	ALERT_CONFIG	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736717 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:41:57.103445","updatedAt":"2026-07-22T21:41:57.103447"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:41:57.104759
11	DELETE	1	DELETE ALERT_CONFIG for 5	2026-07-22 21:41:57.154977	5	ALERT_CONFIG	\N	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736717 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:41:57.103445","updatedAt":"2026-07-22T21:41:57.103447"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:41:57.15498
12	CREATE	1	CREATE ALERT_CONFIG for 6	2026-07-22 21:42:12.266795	6	ALERT_CONFIG	{"id":6,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diagnostic-e2e","description":"temporary diagnostic","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:42:12.264772","updatedAt":"2026-07-22T21:42:12.264776"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:42:12.2668
13	DELETE	1	DELETE ALERT_CONFIG for 6	2026-07-22 21:42:12.319184	6	ALERT_CONFIG	\N	{"id":6,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diagnostic-e2e","description":"temporary diagnostic","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:42:12.264772","updatedAt":"2026-07-22T21:42:12.264776"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:42:12.319188
14	CREATE	1	CREATE ALERT_CONFIG for 7	2026-07-22 21:42:24.614776	7	ALERT_CONFIG	{"id":7,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736744 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:42:24.613031","updatedAt":"2026-07-22T21:42:24.613032"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:42:24.614779
39	CREATE	1	CREATE ALERT_WORKFLOW for 4	2026-07-22 21:54:12.126739	4	ALERT_WORKFLOW	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 workflow","description":"Temporary diagnostic","alertConfigurationId":10,"notificationChannelIds":"5","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.123356","updatedAt":"2026-07-22T21:54:12.12336"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:54:12.126743
16	DELETE	1	DELETE ALERT_CONFIG for 7	2026-07-22 21:42:24.680291	7	ALERT_CONFIG	\N	{"id":7,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736744 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:42:24.613031","updatedAt":"2026-07-22T21:42:24.613032"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:42:24.680295
17	CREATE	1	CREATE ALERT_CONFIG for 8	2026-07-22 21:44:34.281928	8	ALERT_CONFIG	{"id":8,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.264966","updatedAt":"2026-07-22T21:44:34.264976"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:34.281934
18	UPDATE	1	UPDATE ALERT_CONFIG for 8	2026-07-22 21:44:34.337326	8	ALERT_CONFIG	{"id":8,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 alert updated","description":"Temporary E2E validation alert updated","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.264966","updatedAt":"2026-07-22T21:44:34.335744"}	{"id":8,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.264966","updatedAt":"2026-07-22T21:44:34.264976"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.337327
19	CREATE	1	CREATE NOTIFICATION_CHANNEL for 3	2026-07-22 21:44:34.366308	3	NOTIFICATION_CHANNEL	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 channel","description":"Temporary E2E validation channel","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.362631","updatedAt":"2026-07-22T21:44:34.362635"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:34.36631
20	UPDATE	1	UPDATE NOTIFICATION_CHANNEL for 3	2026-07-22 21:44:34.390535	3	NOTIFICATION_CHANNEL	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 channel updated","description":"Temporary E2E validation channel updated","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.362631","updatedAt":"2026-07-22T21:44:34.390083"}	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 channel","description":"Temporary E2E validation channel","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.362631","updatedAt":"2026-07-22T21:44:34.362635"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.390551
21	CREATE	1	CREATE ALERT_WORKFLOW for 2	2026-07-22 21:44:34.414368	2	ALERT_WORKFLOW	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 workflow","description":"Temporary E2E validation workflow","alertConfigurationId":8,"notificationChannelIds":"3","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.411516","updatedAt":"2026-07-22T21:44:34.411519"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:34.414373
22	UPDATE	1	UPDATE ALERT_WORKFLOW for 2	2026-07-22 21:44:34.450438	2	ALERT_WORKFLOW	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 workflow updated","description":"Temporary E2E validation workflow updated","alertConfigurationId":8,"notificationChannelIds":"3","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.411516","updatedAt":"2026-07-22T21:44:34.450115"}	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 workflow","description":"Temporary E2E validation workflow","alertConfigurationId":8,"notificationChannelIds":"3","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.411516","updatedAt":"2026-07-22T21:44:34.411519"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.45044
23	DELETE	1	DELETE ALERT_WORKFLOW for 2	2026-07-22 21:44:34.473101	2	ALERT_WORKFLOW	\N	{"id":2,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 workflow updated","description":"Temporary E2E validation workflow updated","alertConfigurationId":8,"notificationChannelIds":"3","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.411516","updatedAt":"2026-07-22T21:44:34.452533"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.473105
24	DELETE	1	DELETE NOTIFICATION_CHANNEL for 3	2026-07-22 21:44:34.492941	3	NOTIFICATION_CHANNEL	\N	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 channel updated","description":"Temporary E2E validation channel updated","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.362631","updatedAt":"2026-07-22T21:44:34.392203"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.492942
25	DELETE	1	DELETE ALERT_CONFIG for 8	2026-07-22 21:44:34.50939	8	ALERT_CONFIG	\N	{"id":8,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736874 alert updated","description":"Temporary E2E validation alert updated","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:34.264966","updatedAt":"2026-07-22T21:44:34.339121"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:34.509393
26	CREATE	1	CREATE ALERT_CONFIG for 9	2026-07-22 21:44:49.615398	9	ALERT_CONFIG	{"id":9,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.612927","updatedAt":"2026-07-22T21:44:49.612936"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:49.615401
52	CREATE	7	CREATE ALERT_WORKFLOW for 5	2026-07-24 21:47:30.307759	5	ALERT_WORKFLOW	{"id":5,"applicationId":7,"owningAdGrp":"test","name":"High CPU Alert Workflow","description":"Workflow for alert: High CPU Alert","keepWorkflowId":"b30087c0-b692-4a37-9f23-e5adb34e6cae","alertConfigurationId":15,"notificationChannelIds":"slack","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-24T21:47:30.304743","updatedAt":"2026-07-24T21:47:30.304746"}	\N	test	siddharth	\N	ACTIVE	2026-07-24 21:47:30.307761
27	UPDATE	1	UPDATE ALERT_CONFIG for 9	2026-07-22 21:44:49.657818	9	ALERT_CONFIG	{"id":9,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 alert updated","description":"Temporary E2E validation alert updated","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.612927","updatedAt":"2026-07-22T21:44:49.657368"}	{"id":9,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 alert","description":"Temporary E2E validation alert","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.612927","updatedAt":"2026-07-22T21:44:49.612936"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.657821
28	CREATE	1	CREATE NOTIFICATION_CHANNEL for 4	2026-07-22 21:44:49.681572	4	NOTIFICATION_CHANNEL	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 channel","description":"Temporary E2E validation channel","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.678918","updatedAt":"2026-07-22T21:44:49.678923"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:49.681575
29	UPDATE	1	UPDATE NOTIFICATION_CHANNEL for 4	2026-07-22 21:44:49.704112	4	NOTIFICATION_CHANNEL	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 channel updated","description":"Temporary E2E validation channel updated","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.678918","updatedAt":"2026-07-22T21:44:49.703664"}	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 channel","description":"Temporary E2E validation channel","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.678918","updatedAt":"2026-07-22T21:44:49.678923"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.704115
30	CREATE	1	CREATE ALERT_WORKFLOW for 3	2026-07-22 21:44:49.727815	3	ALERT_WORKFLOW	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 workflow","description":"Temporary E2E validation workflow","alertConfigurationId":9,"notificationChannelIds":"4","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.725498","updatedAt":"2026-07-22T21:44:49.725502"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:44:49.727821
31	UPDATE	1	UPDATE ALERT_WORKFLOW for 3	2026-07-22 21:44:49.764506	3	ALERT_WORKFLOW	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 workflow updated","description":"Temporary E2E validation workflow updated","alertConfigurationId":9,"notificationChannelIds":"4","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.725498","updatedAt":"2026-07-22T21:44:49.764093"}	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 workflow","description":"Temporary E2E validation workflow","alertConfigurationId":9,"notificationChannelIds":"4","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.725498","updatedAt":"2026-07-22T21:44:49.725502"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.76451
32	DELETE	1	DELETE ALERT_WORKFLOW for 3	2026-07-22 21:44:49.78554	3	ALERT_WORKFLOW	\N	{"id":3,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 workflow updated","description":"Temporary E2E validation workflow updated","alertConfigurationId":9,"notificationChannelIds":"4","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.725498","updatedAt":"2026-07-22T21:44:49.766741"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.785542
33	DELETE	1	DELETE NOTIFICATION_CHANNEL for 4	2026-07-22 21:44:49.806569	4	NOTIFICATION_CHANNEL	\N	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 channel updated","description":"Temporary E2E validation channel updated","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.678918","updatedAt":"2026-07-22T21:44:49.706035"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.806571
34	DELETE	1	DELETE ALERT_CONFIG for 9	2026-07-22 21:44:49.82816	9	ALERT_CONFIG	\N	{"id":9,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"e2e-1784736889 alert updated","description":"Temporary E2E validation alert updated","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:44:49.612927","updatedAt":"2026-07-22T21:44:49.660643"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:44:49.828161
35	CREATE	1	CREATE ALERT_CONFIG for 10	2026-07-22 21:54:11.990436	10	ALERT_CONFIG	{"id":10,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 alert","description":"Temporary diagnostic","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:54:11.964494","updatedAt":"2026-07-22T21:54:11.96451"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:54:11.990442
36	UPDATE	1	UPDATE ALERT_CONFIG for 10	2026-07-22 21:54:12.043169	10	ALERT_CONFIG	{"id":10,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 alert updated","description":"Temporary diagnostic","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:54:11.964494","updatedAt":"2026-07-22T21:54:12.042221"}	{"id":10,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 alert","description":"Temporary diagnostic","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu_usage \\u003e 90","enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T21:54:11.964494","updatedAt":"2026-07-22T21:54:11.96451"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.043174
37	CREATE	1	CREATE NOTIFICATION_CHANNEL for 5	2026-07-22 21:54:12.072796	5	NOTIFICATION_CHANNEL	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 channel","description":"Temporary diagnostic","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.068564","updatedAt":"2026-07-22T21:54:12.068568"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 21:54:12.072806
38	UPDATE	1	UPDATE NOTIFICATION_CHANNEL for 5	2026-07-22 21:54:12.102572	5	NOTIFICATION_CHANNEL	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 channel updated","description":"Temporary diagnostic","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.068564","updatedAt":"2026-07-22T21:54:12.102098"}	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 channel","description":"Temporary diagnostic","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":false,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.068564","updatedAt":"2026-07-22T21:54:12.068568"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.102588
40	UPDATE	1	UPDATE ALERT_WORKFLOW for 4	2026-07-22 21:54:12.155229	4	ALERT_WORKFLOW	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 workflow updated","description":"Temporary diagnostic","alertConfigurationId":10,"notificationChannelIds":"5","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.123356","updatedAt":"2026-07-22T21:54:12.154768"}	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 workflow","description":"Temporary diagnostic","alertConfigurationId":10,"notificationChannelIds":"5","isActive":true,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.123356","updatedAt":"2026-07-22T21:54:12.12336"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.155231
41	DELETE	1	DELETE ALERT_WORKFLOW for 4	2026-07-22 21:54:12.180367	4	ALERT_WORKFLOW	\N	{"id":4,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 workflow updated","description":"Temporary diagnostic","alertConfigurationId":10,"notificationChannelIds":"5","isActive":false,"executionCount":0,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.123356","updatedAt":"2026-07-22T21:54:12.158732"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.180371
42	DELETE	1	DELETE NOTIFICATION_CHANNEL for 5	2026-07-22 21:54:12.200043	5	NOTIFICATION_CHANNEL	\N	{"id":5,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 channel updated","description":"Temporary diagnostic","channelType":"WEBHOOK","endpoint":"http://localhost:9999/test","isDefault":true,"status":"ACTIVE","createdAt":"2026-07-22T21:54:12.068564","updatedAt":"2026-07-22T21:54:12.104804"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.200045
43	DELETE	1	DELETE ALERT_CONFIG for 10	2026-07-22 21:54:12.218938	10	ALERT_CONFIG	\N	{"id":10,"applicationId":1,"serviceId":1,"owningAdGrp":"dev-team","name":"diag-1784737451 alert updated","description":"Temporary diagnostic","alertType":"METRIC","severity":"CRITICAL","conditionExpression":"cpu_usage \\u003e 95","enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T21:54:11.964494","updatedAt":"2026-07-22T21:54:12.045386"}	dev-team	siddharth	1	ACTIVE	2026-07-22 21:54:12.218942
44	CREATE	1	CREATE ALERT_CONFIG for 11	2026-07-22 22:23:17.56502	11	ALERT_CONFIG	{"id":11,"applicationId":1,"owningAdGrp":"dev-team","name":"Test E2E Alert","description":"Full field test","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu \\u003e 90","environment":"production","source":"prometheus","channels":"EMAIL,SLACK,TEAMS","goalertServiceUrl":"https://goalert.example.com/api/svc/abc123","teamsWebhookUrl":"https://outlook.office.com/webhook/xyz","triggerAiInvestigation":true,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T22:23:17.555887","updatedAt":"2026-07-22T22:23:17.555893"}	\N	dev-team	siddharth	\N	ACTIVE	2026-07-22 22:23:17.565022
45	UPDATE	1	UPDATE ALERT_CONFIG for 11	2026-07-22 22:23:29.345693	11	ALERT_CONFIG	{"id":11,"applicationId":1,"owningAdGrp":"dev-team","name":"Test E2E Alert Updated","alertType":"METRIC","severity":"LOW","conditionExpression":"cpu \\u003e 80","environment":"staging","source":"datadog","channels":"EMAIL","goalertServiceUrl":"https://goalert.example.com/x","teamsWebhookUrl":"https://outlook.office.com/y","triggerAiInvestigation":false,"enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T22:23:17.555887","updatedAt":"2026-07-22T22:23:29.344008"}	{"id":11,"applicationId":1,"owningAdGrp":"dev-team","name":"Test E2E Alert","description":"Full field test","alertType":"METRIC","severity":"HIGH","conditionExpression":"cpu \\u003e 90","environment":"production","source":"prometheus","channels":"EMAIL,SLACK,TEAMS","goalertServiceUrl":"https://goalert.example.com/api/svc/abc123","teamsWebhookUrl":"https://outlook.office.com/webhook/xyz","triggerAiInvestigation":true,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T22:23:17.555887","updatedAt":"2026-07-22T22:23:17.555893"}	dev-team	siddharth	\N	ACTIVE	2026-07-22 22:23:29.345696
46	DELETE	1	DELETE ALERT_CONFIG for 11	2026-07-22 22:23:29.379876	11	ALERT_CONFIG	\N	{"id":11,"applicationId":1,"owningAdGrp":"dev-team","name":"Test E2E Alert Updated","alertType":"METRIC","severity":"LOW","conditionExpression":"cpu \\u003e 80","environment":"staging","source":"datadog","channels":"EMAIL","goalertServiceUrl":"https://goalert.example.com/x","teamsWebhookUrl":"https://outlook.office.com/y","triggerAiInvestigation":false,"enabled":false,"status":"ACTIVE","createdAt":"2026-07-22T22:23:17.555887","updatedAt":"2026-07-22T22:23:29.347574"}	dev-team	siddharth	\N	ACTIVE	2026-07-22 22:23:29.379882
47	CREATE	1	CREATE ALERT_CONFIG for 12	2026-07-22 22:28:50.93015	12	ALERT_CONFIG	{"id":12,"applicationId":1,"owningAdGrp":"","name":"Playwright E2E Test Alert","description":"","alertType":"METRIC","severity":"MEDIUM","conditionExpression":"cpu_usage \\u003e 99","environment":"production","source":"prometheus","channels":"SLACK,TEAMS","goalertServiceUrl":"https://goalert.example.com/api/svc/test","teamsWebhookUrl":"https://outlook.office.com/webhook/test","triggerAiInvestigation":true,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T22:28:50.926858","updatedAt":"2026-07-22T22:28:50.926863"}	\N		siddharth	\N	ACTIVE	2026-07-22 22:28:50.930153
48	DELETE	1	DELETE ALERT_CONFIG for 12	2026-07-22 22:29:00.397328	12	ALERT_CONFIG	\N	{"id":12,"applicationId":1,"owningAdGrp":"","name":"Playwright E2E Test Alert","description":"","alertType":"METRIC","severity":"MEDIUM","conditionExpression":"cpu_usage \\u003e 99","environment":"production","source":"prometheus","channels":"SLACK,TEAMS","goalertServiceUrl":"https://goalert.example.com/api/svc/test","teamsWebhookUrl":"https://outlook.office.com/webhook/test","triggerAiInvestigation":true,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-22T22:28:50.926858","updatedAt":"2026-07-22T22:28:50.926863"}		siddharth	\N	ACTIVE	2026-07-22 22:29:00.397331
49	CREATE	3	CREATE ALERT_CONFIG for 13	2026-07-23 18:18:14.047334	13	ALERT_CONFIG	{"id":13,"applicationId":3,"serviceId":123,"owningAdGrp":"aap-team","name":"Stuck Alert","description":"","alertType":"EVENT","severity":"LOW","conditionExpression":"time\\u003e30min","environment":"staging","source":"system","channels":"TEAMS,EMAIL,GOALERT","goalertServiceUrl":"","teamsWebhookUrl":"","triggerAiInvestigation":true,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-23T18:18:14.041338","updatedAt":"2026-07-23T18:18:14.04134"}	\N	aap-team	siddharth	\N	ACTIVE	2026-07-23 18:18:14.047336
50	CREATE	5	CREATE ALERT_CONFIG for 14	2026-07-24 21:44:17.05446	14	ALERT_CONFIG	{"id":14,"applicationId":5,"owningAdGrp":"test-team","name":"High CPU Usage Alert","description":"Triggers when CPU usage exceeds 80% for 5 minutes","alertType":"THRESHOLD","severity":"WARNING","conditionExpression":"cpu_usage \\u003e 80","environment":"production","source":"prometheus","channels":"slack,pagerduty","triggerAiInvestigation":false,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-24T21:44:17.047443","updatedAt":"2026-07-24T21:44:17.047445"}	\N	test-team	siddharth	\N	ACTIVE	2026-07-24 21:44:17.054466
51	CREATE	7	CREATE ALERT_CONFIG for 15	2026-07-24 21:47:30.252306	15	ALERT_CONFIG	{"id":15,"applicationId":7,"owningAdGrp":"test","name":"High CPU Alert","description":"CPU alert","alertType":"THRESHOLD","severity":"WARNING","conditionExpression":"cpu\\u003e80","environment":"prod","source":"prometheus","channels":"slack","triggerAiInvestigation":false,"enabled":true,"status":"ACTIVE","createdAt":"2026-07-24T21:47:30.246386","updatedAt":"2026-07-24T21:47:30.246393"}	\N	test	siddharth	\N	ACTIVE	2026-07-24 21:47:30.252309
\.


--
-- TOC entry 3520 (class 0 OID 17367)
-- Dependencies: 225
-- Data for Name: keep_resource_mappings; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.keep_resource_mappings (id, application_id, created_at, keep_resource_id, keep_resource_name, local_id, metadata, owning_ad_grp, resource_type, service_id, status, updated_at) FROM stdin;
\.


--
-- TOC entry 3522 (class 0 OID 17376)
-- Dependencies: 227
-- Data for Name: notification_channel; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.notification_channel (id, application_id, channel_type, configuration, created_at, description, endpoint, is_default, name, owning_ad_grp, service_id, status, updated_at, keep_provider_id) FROM stdin;
1	1	SLACK	{"channel":"#prod-alerts"}	2026-07-21 21:14:34.51376	Slack channel for critical alerts (prod)	https://hooks.slack.com/services/AAA/BBB/CCC	t	Slack Alerts - Prod	dev-team	1	ACTIVE	2026-07-21 21:14:39.706734	\N
\.


--
-- TOC entry 3510 (class 0 OID 16388)
-- Dependencies: 215
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: fixora_user
--

COPY public.users (id, name, email, ad_group, created_at, updated_at) FROM stdin;
1	Admin User	admin@example.com	admin-group	2026-07-21 15:42:36.685387	2026-07-21 15:42:36.685387
2	User One	user1@example.com	dev-team	2026-07-21 15:42:36.685387	2026-07-21 15:42:36.685387
\.


--
-- TOC entry 3535 (class 0 OID 0)
-- Dependencies: 218
-- Name: alert_config_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.alert_config_id_seq', 15, true);


--
-- TOC entry 3536 (class 0 OID 0)
-- Dependencies: 220
-- Name: alert_workflow_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.alert_workflow_id_seq', 5, true);


--
-- TOC entry 3537 (class 0 OID 0)
-- Dependencies: 216
-- Name: applications_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.applications_id_seq', 7, true);


--
-- TOC entry 3538 (class 0 OID 0)
-- Dependencies: 222
-- Name: audit_events_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.audit_events_id_seq', 52, true);


--
-- TOC entry 3539 (class 0 OID 0)
-- Dependencies: 224
-- Name: keep_resource_mappings_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.keep_resource_mappings_id_seq', 1, false);


--
-- TOC entry 3540 (class 0 OID 0)
-- Dependencies: 226
-- Name: notification_channel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.notification_channel_id_seq', 5, true);


--
-- TOC entry 3541 (class 0 OID 0)
-- Dependencies: 214
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fixora_user
--

SELECT pg_catalog.setval('public.users_id_seq', 2, true);


--
-- TOC entry 3352 (class 2606 OID 17335)
-- Name: alert_config alert_config_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config
    ADD CONSTRAINT alert_config_pkey PRIMARY KEY (id);


--
-- TOC entry 3354 (class 2606 OID 17344)
-- Name: alert_workflow alert_workflow_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT alert_workflow_pkey PRIMARY KEY (id);


--
-- TOC entry 3346 (class 2606 OID 16413)
-- Name: applications applications_alias_key; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_alias_key UNIQUE (alias);


--
-- TOC entry 3348 (class 2606 OID 17347)
-- Name: applications applications_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.applications
    ADD CONSTRAINT applications_pkey PRIMARY KEY (id);


--
-- TOC entry 3356 (class 2606 OID 17365)
-- Name: audit_events audit_events_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events
    ADD CONSTRAINT audit_events_pkey PRIMARY KEY (id);


--
-- TOC entry 3358 (class 2606 OID 17374)
-- Name: keep_resource_mappings keep_resource_mappings_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings
    ADD CONSTRAINT keep_resource_mappings_pkey PRIMARY KEY (id);


--
-- TOC entry 3360 (class 2606 OID 17383)
-- Name: notification_channel notification_channel_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel
    ADD CONSTRAINT notification_channel_pkey PRIMARY KEY (id);


--
-- TOC entry 3342 (class 2606 OID 16399)
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- TOC entry 3344 (class 2606 OID 17386)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3349 (class 1259 OID 16415)
-- Name: idx_applications_alias; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_applications_alias ON public.applications USING btree (alias);


--
-- TOC entry 3350 (class 1259 OID 16416)
-- Name: idx_applications_status; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_applications_status ON public.applications USING btree (status);


--
-- TOC entry 3340 (class 1259 OID 16414)
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: fixora_user
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- TOC entry 3362 (class 2606 OID 17400)
-- Name: alert_workflow fk6jnah8qpv0rs5lknvjp4327qc; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT fk6jnah8qpv0rs5lknvjp4327qc FOREIGN KEY (alert_configuration_id) REFERENCES public.alert_config(id);


--
-- TOC entry 3366 (class 2606 OID 17420)
-- Name: notification_channel fk8i79mtb80tego5ap3guctab3l; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.notification_channel
    ADD CONSTRAINT fk8i79mtb80tego5ap3guctab3l FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- TOC entry 3363 (class 2606 OID 17405)
-- Name: alert_workflow fkc82wjme8x83ga8ptgosi5k7om; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_workflow
    ADD CONSTRAINT fkc82wjme8x83ga8ptgosi5k7om FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- TOC entry 3365 (class 2606 OID 17415)
-- Name: keep_resource_mappings fkfyw1ssi4apdo1o3olla26cl27; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.keep_resource_mappings
    ADD CONSTRAINT fkfyw1ssi4apdo1o3olla26cl27 FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- TOC entry 3361 (class 2606 OID 17395)
-- Name: alert_config fkpeee8uo8ogvu4hm1kfjqloesw; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.alert_config
    ADD CONSTRAINT fkpeee8uo8ogvu4hm1kfjqloesw FOREIGN KEY (application_id) REFERENCES public.applications(id);


--
-- TOC entry 3364 (class 2606 OID 17410)
-- Name: audit_events fkr2nbs0h3djuplae34p7aua3b0; Type: FK CONSTRAINT; Schema: public; Owner: fixora_user
--

ALTER TABLE ONLY public.audit_events
    ADD CONSTRAINT fkr2nbs0h3djuplae34p7aua3b0 FOREIGN KEY (application_id) REFERENCES public.applications(id);


-- Completed on 2026-07-24 21:54:50 IST

--
-- PostgreSQL database dump complete
--

\unrestrict BxoDVPqGlWMO3Caqbrcmchd8cQX04HGL9pob4K9CmnnlPiQVCCFjUeN20yD0Q5E

