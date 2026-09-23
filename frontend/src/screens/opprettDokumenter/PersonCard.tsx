import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import {
  BodyShort,
  Button,
  Detail,
  ErrorMessage,
  Heading,
  HStack,
  Label,
  List,
  VStack,
} from "@navikt/ds-react";

import { FormInput } from "../../components/FormInput";
import { FormSelect } from "../../components/FormSelect";
import { Checkbox } from "../../components/Checkbox";
import { Card } from "../../components/Card";
import { FetchButton } from "../../components/FetchButton";
import { SykdomTom } from "./SykdomTom";
import { SykdomFom } from "./SykdomFom";
import { DeleteButton } from "./DeleteButton";
import { ArbeidssituasjonDTO } from "../../utils/types";
import { get } from "../../io/api";
import {
  validateFødselsnummer,
  validateOrganisasjonsnummer,
} from "../formValidation";

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
  type: string;
  arbeidsgiver: {
    type: string;
    identifikator: string;
  };
  ansattFom: string;
  detaljer: Arbeidsforholddetalje[];
}
interface Arbeidsforholddetalje {
  yrke: string;
}

function lagreSøk(fnr: string, navn: string) {
  if (!localStorage.hasOwnProperty("historikk"))
    localStorage.historikk = '{ "historikk": [] }';
  const historikk = JSON.parse(localStorage.historikk);
  const navnesøk = historikk.historikk as { navn: string; fnr: string }[];
  if (navnesøk.findIndex((it) => it.fnr === fnr) != -1) return;
  navnesøk.push({
    fnr: fnr,
    navn: navn,
  });
  localStorage.historikk = JSON.stringify(historikk);
}

export const PersonCard = ({
  setErArbeidstaker,
  setPersonIkkeFunnet,
  status,
  isFetching,
  isPersonNotFound,
  errorBody,
}: {
  setErArbeidstaker: (value: boolean) => void;
  setPersonIkkeFunnet: (value: boolean) => void;
  status?: number;
  isFetching: boolean;
  isPersonNotFound: boolean;
  errorBody?: string;
}) => {
  const { register, formState, watch } = useFormContext();
  const [deleteErrorMessage, setDeleteErrorMessage] = useState<
    string | undefined
  >(undefined);
  const [isChecked, setIsChecked] = useState(true);
  const [personIkkeFunnet, setLocalPersonIkkeFunnet] = useState(false);

  const oppdaterPersonIkkeFunnet = (verdi: boolean) => {
    setLocalPersonIkkeFunnet(verdi);
    setPersonIkkeFunnet(verdi);
  };

  const validateSendsDocuments = useDocumentsValidator();
  const fnr = watch("fnr");
  const arbeidssituasjon: ArbeidssituasjonDTO = watch("arbeidssituasjon");
  const skalKreveOrgnummer = arbeidssituasjon === "ARBEIDSTAKER";

  const [arbeidsgivere, setArbeidsgivere] = useState([] as Arbeidsgiver[]);
  const [navn, setNavn] = useState<string | null>(null);

  useEffect(() => {
    if (!fnr || fnr.length < 11) {
      setNavn(null);
      oppdaterPersonIkkeFunnet(false);
      return setArbeidsgivere([]);
    }
    get(`/person/${fnr}`)
      .then((result) => result.json())
      .then((json) => {
        if (json.type === "urn:error:not_found") {
          oppdaterPersonIkkeFunnet(true);
          return;
        }
        oppdaterPersonIkkeFunnet(false);
        if (typeof json.fornavn === "undefined") return;
        const navn = `${json.fornavn}${json.mellomnavn ? ` ${json.mellomnavn}` : ""} ${json.etternavn}`;
        lagreSøk(fnr, navn);
        setNavn(navn);
      });
    get("/person/arbeidsforhold", { ident: fnr })
      .then((result) => result.json())
      .then((response) => {
        if (typeof response.arbeidsforhold === "undefined")
          return console.log(`ukjent response: `, response);
        setArbeidsgivere(() =>
          response.arbeidsforhold.map(
            (it: {
              type: string;
              arbeidsgiver: { type: string; identifikator: string };
              ansettelseperiodeFom: string;
              detaljer: { yrke: string }[];
            }) => {
              return {
                type: it.type,
                arbeidsgiver: {
                  type: it.arbeidsgiver.type,
                  identifikator: it.arbeidsgiver.identifikator,
                },
                ansattFom: it.ansettelseperiodeFom,
                detaljer: it.detaljer.map((detalje: { yrke: string }) => {
                  return {
                    yrke: detalje.yrke,
                  };
                }),
              } as Arbeidsgiver;
            },
          ),
        );
      });
  }, [fnr]);

  const deleteFailed = (errorMessage: string | null) => {
    setDeleteErrorMessage(errorMessage ?? undefined);
  };

  function getSendIMChecked(isChecked: boolean) {
    const erArbeidstaker =
      arbeidssituasjon === "ARBEIDSTAKER" ? isChecked : false;
    setErArbeidstaker(erArbeidstaker);
    return erArbeidstaker;
  }

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Person
        </Heading>
        <VStack gap="space-16">
          <HStack gap="space-8" align="end" justify="space-between">
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
          </HStack>
          <ErrorMessage size="small">{deleteErrorMessage}</ErrorMessage>
        </VStack>
        {personIkkeFunnet && (
          <ErrorMessage>Person ikke funnet i PDL</ErrorMessage>
        )}
        {navn && <Detail>{navn}</Detail>}
        {skalKreveOrgnummer ? (
          <>
            <FormInput
              data-testid="orgnummer"
              label="Organisasjonsnummer"
              errors={formState.errors}
              {...register("orgnummer", {
                required: "Organisasjonsnummer må fylles ut",
                validate: validateOrganisasjonsnummer,
                shouldUnregister: true,
              })}
            />
            {arbeidsgivere.length > 0 && (
              <Arbeidsgivere arbeidsgivere={arbeidsgivere} />
            )}
          </>
        ) : (
          <div>
            <Label size="small">Organisasjonsnummer</Label>
            <Detail>Kun aktuelt ved IM/arb.tak.søknad</Detail>
          </div>
        )}
        <SykdomFom />
        <SykdomTom />
        <FormSelect
          label="Arbeidssituasjon"
          options={[
            "ARBEIDSTAKER",
            "ARBEIDSLEDIG",
            "FRILANSER",
            "JORDBRUKER",
            "FISKER",
            "SELVSTENDIG_NARINGSDRIVENDE",
            "BARNEPASSER",
          ]}
          {...register("arbeidssituasjon")}
        />
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
        <VStack gap="space-16">
          <FetchButton
            status={status}
            isFetching={isFetching}
            type="submit"
            disabled={isPersonNotFound}
          >
            Opprett dokumenter
          </FetchButton>
          {typeof status === "number" && status >= 400 && (
            <ErrorMessage>
              Noe gikk galt! Melding fra server: {errorBody}
            </ErrorMessage>
          )}
        </VStack>
        {typeof validateSendsDocuments() === "string" && (
          <ErrorMessage size="small">{validateSendsDocuments()}</ErrorMessage>
        )}
        <TidligereSøk />
      </VStack>
    </Card>
  );
};

type Historikk = {
  historikk: { fnr: string; navn: string }[];
};

function TidligereSøk() {
  const initialHistorikk = localStorage.hasOwnProperty("historikk")
    ? JSON.parse(localStorage.historikk)
    : null;
  const [historikk, setHistorikk] = useState<Historikk | null>(
    initialHistorikk,
  );
  if (historikk == null) return null;

  return (
    <VStack gap="space-8" align="start">
      <Heading level="3" size="xsmall">
        Tidligere søk
      </Heading>
      <List size="small">
        {historikk.historikk.map((it, i) => (
          <List.Item key={i}>
            {it.fnr}: {it.navn}
          </List.Item>
        ))}
      </List>
      <Button
        type="button"
        variant="secondary"
        size="small"
        onClick={() => {
          localStorage.removeItem("historikk");
          setHistorikk(null);
        }}
      >
        Tøm historikk
      </Button>
    </VStack>
  );
}

interface OrganisasjonResponse {
  navn: string;
}
function Arbeidsgivere({ arbeidsgivere }: { arbeidsgivere: Arbeidsgiver[] }) {
  const [arbeidsgivernavn, setArbeidsgivernavn] = useState(
    arbeidsgivere.map(() => ({ navn: "ukjent" })) as OrganisasjonResponse[],
  );

  useEffect(() => {
    Promise.all([
      ...arbeidsgivere.map((arbeidsgiver) => {
        return get(`/organisasjon/${arbeidsgiver.arbeidsgiver.identifikator}`)
          .then((response) => response.json())
          .then((json) => {
            if (typeof json.navn !== "undefined")
              return { navn: json.navn } as OrganisasjonResponse;
            return { navn: "[ukjent]" } as OrganisasjonResponse;
          })
          .catch((error) => {
            console.log(
              `Fikk feil ved oppslag av organisasjon ${arbeidsgiver.arbeidsgiver.identifikator}: ${error}`,
            );
            return {
              navn: "[fikk feil]",
            } as OrganisasjonResponse;
          });
      }),
    ]).then((result) => {
      setArbeidsgivernavn(result);
    });
  }, [arbeidsgivere]);

  return (
    <div>
      <Detail>Registrerte arbeidsforhold:</Detail>
      <List size="small">
        {arbeidsgivere.map((it, i) => (
          <List.Item key={i}>
            <BodyShort size="small">{arbeidsgivernavn[i].navn}</BodyShort>
            <Detail>
              {it.arbeidsgiver.identifikator} ({it.detaljer[0].yrke}, fom.{" "}
              {it.ansattFom})
            </Detail>
          </List.Item>
        ))}
      </List>
    </div>
  );
}
