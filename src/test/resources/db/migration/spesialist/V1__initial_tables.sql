create table person
(
    id serial not null constraint person_pkey primary key,
    fodselsnummer bigint not null,
    aktor_id bigint not null
);

create table speil_snapshot
(
    id serial not null constraint speil_snapshot_pkey primary key
);

create table vedtak
(
    id serial not null constraint vedtak_pkey primary key,
    person_ref integer not null constraint vedtak_person_ref_fkey references person,
    speil_snapshot_ref integer not null constraint vedtak_speil_snapshot_ref_fkey references speil_snapshot
);

create table oppgave
(
    id serial not null constraint oppgave_pkey primary key,
    vedtak_ref integer constraint oppgave_vedtak_ref_fkey references vedtak
);

