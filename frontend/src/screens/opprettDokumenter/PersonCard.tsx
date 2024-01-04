import styles from "./OpprettDokumenter.module.css";
import {FormInput} from "../../components/FormInput";
import {Checkbox} from "../../components/Checkbox";
import {Card} from "../../components/Card";
import {ErrorMessage} from "../../components/ErrorMessage";
import {useFormContext} from "react-hook-form";
import React, {useEffect, useState} from "react";
import {validateFødselsnummer, validateOrganisasjonsnummer,} from "../formValidation";
import {SykdomTom} from "./SykdomTom";
import {SykdomFom} from "./SykdomFom";
import {DeleteButton} from "./DeleteButton";
import {ArbeidssituasjonDTO} from "../../utils/types";
import {get} from "../../io/api";

const useDocumentsValidator = () => {
  const { watch } = useFormContext();

  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const skalSendeSøknad = watch("skalSendeSøknad");
  const skalSendeInntektsmelding = watch("skalSendeInntektsmelding");

  return () =>
    skalSendeSykmelding ||
    skalSendeSøknad ||
    skalSendeInntektsmelding ||
    "Huk av for å sende minst ett dokument";
};

interface Arbeidsgiver {
  type: string,
  arbeidsgiver: {
    type: string
    identifikator: string
  },
  ansattFom: string,
  detaljer: Arbeidsforholddetalje[]
}
interface Arbeidsforholddetalje {
  yrke: string
}

export const PersonCard = () => {
  const { register, unregister, formState, watch } = useFormContext();
  const [deleteErrorMessage, setDeleteErrorMessage] = useState(undefined);

  const validateSendsDocuments = useDocumentsValidator();
  const fnr = watch("fnr")
  const arbeidssituasjon: ArbeidssituasjonDTO = watch("søknad.arbeidssituasjon")
  const skalSendeInntektsmelding = watch("skalSendeInntektsmelding");
  const skalKreveOrgnummer = skalSendeInntektsmelding || arbeidssituasjon === "ARBEIDSTAKER"

  const [arbeidsgivere, setArbeidsgivere] = useState([] as Arbeidsgiver[])

  useEffect(() => {
    if (!fnr || fnr.length < 11) return setArbeidsgivere([])
    get("/person/arbeidsforhold", { ident: fnr })
        .then((result) => result.json() )
        .then((response) => {
          if (typeof response.arbeidsforhold === 'undefined') return console.log(`ukjent response: `, response)
          setArbeidsgivere(() =>  response.arbeidsforhold.map((it) => {
            return {
              type: it.type,
              arbeidsgiver: {
                type: it.arbeidsgiver.type,
                identifikator: it.arbeidsgiver.identifikator
              },
              ansattFom: it.ansettelseperiodeFom,
              detaljer: it.detaljer.map((detalje) => {
                return {
                  yrke: detalje.yrke
                }
              })
            } as Arbeidsgiver
          }))
        })
  }, [fnr]);

  const deleteFailed = (errorMessage: string) => {
    setDeleteErrorMessage(errorMessage);
  };

  return (
    <Card>
      <h2 className={styles.Title}>Person</h2>
      <div className={styles.CardContainer}>
        <span className={styles.Fødselsnummer}>
          <FormInput
            data-testid="fnr"
            label="Fødselsnummer"
            errors={formState.errors}
            {...register("fnr", {
              required: "Fødselsnummer må fylles ut",
              validate: validateFødselsnummer,
            })}
          />
          <DeleteButton errorCallback={deleteFailed} />
        </span>
        {skalKreveOrgnummer && <>
          <FormInput
            data-testid="orgnummer"
            label="Organisasjonsnummer"
            errors={formState.errors}
            {...register("orgnummer", {
              required: "Organisasjonsnummer må fylles ut",
              validate: validateOrganisasjonsnummer,
              shouldUnregister: true
            })}
          />
          { arbeidsgivere.length > 0 && <Arbeidsgivere arbeidsgivere={arbeidsgivere} /> }
        </>}
        <SykdomFom />
        <SykdomTom />
        <Checkbox
          label="Send sykmelding"
          {...register("skalSendeSykmelding", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        <Checkbox
          label="Send søknad"
          {...register("skalSendeSøknad", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        <Checkbox
          label="Send inntektsmelding"
          {...register("skalSendeInntektsmelding", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        {typeof validateSendsDocuments() === "string" && (
          <ErrorMessage className={styles.DocumentError}>
            {validateSendsDocuments()}
          </ErrorMessage>
        )}
        {/* Rendres alltid for å unngå resizing av card-et, som har width: max-content */}
        <ErrorMessage className={styles.DocumentError}>
          {deleteErrorMessage}
        </ErrorMessage>
      </div>
    </Card>
  );
};

interface OrganisasjonResponse {
  navn: string
}
function Arbeidsgivere({ arbeidsgivere }: { arbeidsgivere: Arbeidsgiver[] }) {
  const [arbeidsgivernavn, setArbeidsgivernavn] = useState(arbeidsgivere.map(() => ({ navn: "ukjent" })) as OrganisasjonResponse[])

  useEffect(() => {
    Promise.all([...arbeidsgivere.map((arbeidsgiver) => {
      return get(`/organisasjon/${arbeidsgiver.arbeidsgiver.identifikator}`)
          .then((response) => response.json())
          .then((json) => {
            if (typeof json.navn !== 'undefined') return { navn: json.navn } as OrganisasjonResponse
            return { navn: "[ukjent]" } as OrganisasjonResponse
          })
          .catch((error) => {
            console.log(`Fikk feil ved oppslag av organisasjon ${arbeidsgiver.arbeidsgiver.identifikator}: ${error}`)
            return {
              navn: "[fikk feil]"
            } as OrganisasjonResponse
          })
    })]).then((result) => {
      setArbeidsgivernavn(result)
    })
  }, [arbeidsgivere]);

  return <small>Registrerte arbeidsforhold: <ul>
    {arbeidsgivere.map((it, i) => {
    return <li key={i}>{ arbeidsgivernavn[i].navn }:<br />{ it.arbeidsgiver.identifikator } ({ it.detaljer[0].yrke }, fom. { it.ansattFom })</li>
  })}</ul></small>
}
