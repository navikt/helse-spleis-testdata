create table person
(
    id serial not null constraint person_pkey primary key,
    fodselsnummer bigint not null,
    aktor_id bigint not null
);

create table speil_snapshot
(
    id serial not null constraint speil_snapshot_pkey primary key,
    data VARCHAR(255)
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

create table tildeling
(
    oppgave_id_ref bigint not null references oppgave (id)
);

create table overstyring
(
    id serial not null constraint overstyring_pkey primary key,
    person_ref integer constraint overstyring_person_ref_fkey references person
);

create table overstyrtdag
(
    id serial not null constraint overstyrtdag_pkey primary key,
    overstyring_ref integer constraint overstyrtdag_overstyring_ref_fkey references overstyring
);
