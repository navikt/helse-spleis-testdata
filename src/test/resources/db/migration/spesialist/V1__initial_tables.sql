create table person
(
    id            serial primary key,
    fodselsnummer bigint not null,
    aktor_id      bigint not null
);

create table speil_snapshot
(
    id   serial primary key,
    data VARCHAR(255)
);

create table vedtak
(
    id                 serial primary key,
    person_ref         bigint references person (id)         not null,
    speil_snapshot_ref bigint references speil_snapshot (id) not null
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
