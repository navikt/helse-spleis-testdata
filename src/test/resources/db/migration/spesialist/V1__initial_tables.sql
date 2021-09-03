create table person
(
    id            serial primary key,
    fodselsnummer bigint not null,
    aktor_id      bigint not null
);

create table speil_snapshot
(
    id         serial primary key,
    data       VARCHAR(255),
    person_ref INT UNIQUE REFERENCES person (id)
);

create table vedtak
(
    id                 SERIAL PRIMARY KEY,
    person_ref         BIGINT REFERENCES person (id)         NOT NULL,
    speil_snapshot_ref BIGINT REFERENCES speil_snapshot (id) NOT NULL,
    vedtaksperiode_id  uuid                                  NOT NULL
        CONSTRAINT vedtak_vedtaksperiode_id_key UNIQUE
);

create table oppgave
(
    id         serial primary key,
    vedtak_ref bigint references vedtak (id) not null
);

create table tildeling
(
    oppgave_id_ref bigint references oppgave (id) not null
);

create table overstyring
(
    id         serial primary key,
    person_ref bigint references person (id) not null
);

create table overstyrtdag
(
    id              serial primary key,
    overstyring_ref bigint references overstyring (id) not null
);

create table reserver_person
(
    person_ref bigint references person (id) not null
);

create table automatisering
(
    vedtaksperiode_ref bigint references vedtak (id) primary key
);

create table hendelse
(
    id            serial primary key,
    fodselsnummer bigint not null
);

create table automatisering_problem
(
    id                 serial primary key,
    vedtaksperiode_ref bigint references vedtak (id),
    hendelse_ref       bigint references hendelse (id)
);

create table digital_kontaktinformasjon
(
    person_ref bigint references person (id) not null
);

create table gosysoppgaver
(
    person_ref bigint references person (id) not null
);

create table egen_ansatt
(
    person_ref bigint references person (id) not null
);

create table oppdrag
(
    id serial not null
        constraint oppdrag_pkey primary key
);

create table utbetalingslinje
(
    oppdrag_id bigint not null
        constraint utbetalingslinje_oppdrag_id_fkey references oppdrag
);

create table utbetaling_id
(
    id                            serial  not null
        constraint utbetaling_id_pkey primary key,
    utbetaling_id                 uuid    not null
        constraint utbetaling_id_utbetaling_id_key unique,
    person_ref                    integer not null
        constraint utbetaling_id_person_ref_fkey references person,
    arbeidsgiver_fagsystem_id_ref bigint  not null
        constraint utbetaling_id_arbeidsgiver_fagsystem_id_ref_fkey references oppdrag,
    person_fagsystem_id_ref       bigint  not null
        constraint utbetaling_id_person_fagsystem_id_ref_fkey references oppdrag
);

create table utbetaling
(
    id                serial not null
        constraint utbetaling_pkey primary key,
    utbetaling_id_ref bigint not null
        constraint utbetaling_utbetaling_id_ref_fkey references utbetaling_id
);

CREATE TABLE arbeidsforhold
(
    id         SERIAL PRIMARY KEY,
    person_ref BIGINT NOT NULL REFERENCES person (id)
);

CREATE TABLE saksbehandler
(
    oid   UUID NOT NULL PRIMARY KEY,
    navn  VARCHAR(64),
    epost VARCHAR(128)
);

CREATE TABLE abonnement_for_opptegnelse
(
    saksbehandler_id    UUID   NOT NULL REFERENCES saksbehandler (oid),
    person_id           bigint NOT NULL REFERENCES person (id),
    siste_sekvensnummer integer,
    primary key (saksbehandler_id, person_id)
);

CREATE TABLE opptegnelse
(
    person_id     bigint NOT NULL REFERENCES person (id),
    sekvensnummer SERIAL,
    payload       JSON   NOT NULL,
    type          varchar(64),
    primary key (person_id, sekvensnummer)
);

CREATE TABLE notat
(
    id                SERIAL PRIMARY KEY,
    tekst             VARCHAR(200),
    opprettet         TIMESTAMP DEFAULT now(),
    saksbehandler_oid UUID,
    vedtaksperiode_id UUID NOT NULL
        CONSTRAINT notat_vedtak_ref_fkey REFERENCES vedtak (vedtaksperiode_id),
    CONSTRAINT notat_saksbehandler_ref_fkey FOREIGN KEY (saksbehandler_oid) REFERENCES saksbehandler (oid)
)
