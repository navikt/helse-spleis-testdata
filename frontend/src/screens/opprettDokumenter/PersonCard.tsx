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
import {Button} from "../../components/Button";
import {FormSelect} from "../../components/FormSelect";

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

function lagreSøk(fnr: string, navn: string) {
  if (!localStorage.hasOwnProperty("historikk")) localStorage.historikk = '{ "historikk": [] }'
  const historikk = JSON.parse(localStorage.historikk)
  const navnesøk = historikk.historikk as {navn: string, fnr: string}[]
  if (navnesøk.findIndex((it) => it.fnr === fnr) != -1) return
  navnesøk.push({
    fnr: fnr,
    navn: navn
  })
  localStorage.historikk = JSON.stringify(historikk)
}

export const PersonCard = ({setErArbeidstaker}) => {
  const { register, formState, watch } = useFormContext();
  const [deleteErrorMessage, setDeleteErrorMessage] = useState(undefined);
  const [isChecked, setIsChecked] = useState(true);

  const validateSendsDocuments = useDocumentsValidator();
  const fnr = watch("fnr")
  const arbeidssituasjon: ArbeidssituasjonDTO = watch("arbeidssituasjon")
  const skalKreveOrgnummer =  arbeidssituasjon === "ARBEIDSTAKER"

  const [arbeidsgivere, setArbeidsgivere] = useState([] as Arbeidsgiver[])
  const [navn, setNavn] = useState(null)

  useEffect(() => {
    if (!fnr || fnr.length < 11) {
      setNavn(null)
      return setArbeidsgivere([])
    }
    get(`/person/${fnr}`)
        .then((result) => result.json())
        .then((json) => {
          if (typeof json.fornavn === 'undefined') return
          const navn = `${json.fornavn}${json.mellomnavn ? ` ${json.mellomnavn}` : ''} ${json.etternavn}`
          lagreSøk(fnr, navn)
          setNavn(navn)
        })
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

  function getSendIMChecked(isChecked: boolean) {
    const erArbeidstaker = arbeidssituasjon === "ARBEIDSTAKER" ? isChecked: false
    setErArbeidstaker(erArbeidstaker)
    return erArbeidstaker;
  }

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
        { navn && <small>{ navn }</small> }
        {skalKreveOrgnummer ? <>
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
        </> : <>
          <label className={styles.Infotekst}>Organisasjonsnummer</label>
          <span className={styles.Infotekst}>Kun aktuelt ved IM/arb.tak.søknad</span>
        </>}
        <SykdomFom />
        <SykdomTom />
        <FormSelect
            label="Arbeidssituasjon"
            options={['ARBEIDSTAKER',
              'ARBEIDSLEDIG',
              'FRILANSER',
              'SELVSTENDIG_NARINGSDRIVENDE']}
            {...register("arbeidssituasjon")}
        >
        </FormSelect>
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
          disabled={arbeidssituasjon !== "ARBEIDSTAKER"}
          onClick={() => setIsChecked(!isChecked)}
          checked={getSendIMChecked(isChecked)}
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
        <TidligereSøk />
      </div>
    </Card>
  );
};

type Historikk = {
  historikk: { fnr: string; navn: string }[];
};

function TidligereSøk() {
  const initialHistorikk = localStorage.hasOwnProperty("historikk") ? JSON.parse(localStorage.historikk) : null;
  const [historikk, setHistorikk] = useState<Historikk>(initialHistorikk);
  if (historikk == null) return null;

  return <>
    <h4>Tidligere søk</h4>
    <ul>
      { historikk.historikk.map((it, i) =>
        <li key={i}>{it.fnr}: { it.navn }</li>
      )}
    </ul>
    <Button type="button" onClick={() => {
      localStorage.removeItem("historikk");
      setHistorikk(null);
    }}>Tøm historikk</Button>
  </>
}

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
