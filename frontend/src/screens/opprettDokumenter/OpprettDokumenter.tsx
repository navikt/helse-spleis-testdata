import styles from "./OpprettDokumenter.module.css";
import React, { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";

import { post } from "../../io/api";
import { useSubscribe } from "../../io/subscription";

import { DiverseCard } from "./DiverseCard";
import { PersonCard } from "./PersonCard";
import { SøknadCard } from "./SøknadCard";
import { Ferieperioder } from "./Ferieperioder";
import { EndringRefusjon } from "./EndringRefusjon";
import { InntektsmeldingCard } from "./InntektsmeldingCard";
import { Arbeidsgiverperioder } from "./Arbeidsgiverperioder";

import { FetchButton } from "../../components/FetchButton";
import { ErrorMessage } from "../../components/ErrorMessage";

import type {
  FellesDTO,
  InntektsmeldingDTO,
  PersonDTO,
  SykmeldingDTO,
  SøknadDTO,
} from "../../io/api.d";
import { Egenmeldingsdager } from "./Egenmeldingsdager";
import { InntektFraNyttArbeidsforhold } from "./InntektFraNyttArbeidsforhold";
import { nanoid } from "nanoid";
import { useAddSystemMessage } from "../../state/useSystemMessages";

type OpprettVedtaksperiodePayload = PersonDTO &
  FellesDTO & {
    sykmelding?: SykmeldingDTO;
    søknad?: SøknadDTO;
    medlemskapVerdi: String;
  };

type ArbeidsgiverSvarerPayload = PersonDTO &
    FellesDTO & {
    inntektsmelding?: InntektsmeldingDTO;
};

const createOpprettVedtaksperiodePayload = (
  values: Record<string, any>,
): OpprettVedtaksperiodePayload => {
  const sykmelding = (): SykmeldingDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad,
  });

  const søknad = (): SøknadDTO => {
    let fraværFørSykmeldingen: boolean | null = null;
    switch (values.søknad.fraværFørSykmeldingen) {
      case "Ja":
        fraværFørSykmeldingen = true;
        break;
      case "Nei":
        fraværFørSykmeldingen = false;
        break;
    }
    return {
      sykmeldingsgrad: values.sykmeldingsgrad ?? values.søknad.sykmeldingsgrad,
      harAndreInntektskilder: values.søknad.harAndreInntektskilder ?? false,
      ferieperioder:
        values.søknad.ferieperioder?.map(
          (it: { fom: string; tom: string }) => ({ fom: it.fom, tom: it.tom }),
        ) ?? [],
      egenmeldingsdagerFraSykmelding: values.søknad.egenmeldingsdager,
      faktiskgrad: values.søknad.faktiskgrad || undefined,
      sendtNav: values.søknad.sendtNav || undefined,
      sendtArbeidsgiver: values.søknad.sendtArbeidsgiver || undefined,
      arbeidGjenopptatt: values.søknad.arbeidGjenopptatt || undefined,
      inntektFraNyttArbeidsforhold:
        values.søknad.inntektFraNyttArbeidsforhold || undefined,
      tidligereArbeidsgiverOrgnummer:
        values.søknad.tidligereArbeidsgiverOrgnummer || null,
      inntektFraSigrun: values.søknad.inntektFraSigrun || null,
      fraværFørSykmeldingen: fraværFørSykmeldingen,
      harBrukerOppgittForsikring:
        values.søknad.harBrukerOppgittForsikring || null,
      meldingTilNavDagerFraSykmelding:
        values.søknad.meldingTilNavDagerFraSykmeldingFom &&
        values.søknad.meldingTilNavDagerFraSykmeldingTom
          ? {
              fom: values.søknad.meldingTilNavDagerFraSykmeldingFom,
              tom: values.søknad.meldingTilNavDagerFraSykmeldingTom,
            }
          : null,
    };
  };

  return {
    fnr: values.fnr,
    orgnummer: values.orgnummer || null,
    sykdomFom: values.sykdomFom,
    sykdomTom: values.sykdomTom,
    arbeidssituasjon:
      values.skalSendeSykmelding || values.skalSendeSøknad
        ? values.arbeidssituasjon
        : undefined,
    sykmelding: values.skalSendeSykmelding ? sykmelding() : undefined,
    søknad: values.skalSendeSøknad ? søknad() : undefined,
    medlemskapVerdi: values.medlemskapVerdi,
  };
};

const createArbeidsgiverSvarerPayload = (
    values: Record<string, any>,
    vedtaksperiodeId: string
): ArbeidsgiverSvarerPayload | undefined  => {
    const inntektsmelding = (): InntektsmeldingDTO => ({
        inntekt: values.inntektsmelding.inntekt,
        refusjon: {
            opphørRefusjon: values.inntektsmelding.opphørRefusjon || null,
            refusjonsbeløp: values.inntektsmelding.refusjonsbeløp || null,
        },
        arbeidsgiverperiode:
            values.inntektsmelding.arbeidsgiverperiode?.map(
                (it: { fom: string; tom: string }) => ({ fom: it.fom, tom: it.tom }),
            ) ?? [],
        endringRefusjon:
            values.inntektsmelding.endringIRefusjon?.map(
                (it: { endringsdato: string; endringsbeløp: number }) => ({
                    endringsdato: it.endringsdato,
                    beløp: it.endringsbeløp as number,
                }),
            ) ?? [],
        førsteFraværsdag: values.inntektsmelding.førsteFraværsdag,
        begrunnelseForReduksjonEllerIkkeUtbetalt:
        values.inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt,
        harOpphørAvNaturalytelser:
            values.inntektsmelding.harOpphørAvNaturalytelser ?? false,
        vedtaksperiodeId: vedtaksperiodeId
    });

    if (values.arbeidssituasjon !== "ARBEIDSTAKER" || !values.skalSendeInntektsmelding) return undefined

    return {
        fnr: values.fnr,
        orgnummer: values.orgnummer || null,
        sykdomFom: values.sykdomFom,
        sykdomTom: values.sykdomTom,
        arbeidssituasjon: values.arbeidssituasjon,
        inntektsmelding: inntektsmelding()
    };
};

export const OpprettDokumenter = React.memo(() => {
  const form = useForm({
    defaultValues: {
      skalSendeSykmelding: true,
      skalSendeSøknad: true,
      skalSendeInntektsmelding: true,
    },
    shouldUnregister: true,
  });

  const [erArbeidstaker, setErArbeidstaker] = useState<boolean>(true);

  const skalSendeSøknad = form.watch("skalSendeSøknad");

  const [status, setStatus] = useState<number>();
  const [errorBody, setErrorBody] = useState<string>();
  const [isFetching, setIsFetching] = useState<boolean>(false);

  const [subscribe] = useSubscribe();
  const addMessage = useAddSystemMessage();

  const postOpprettVedtaksperiode = async (data: Record<string, any>): Promise<Response> => {
    return post("/vedtaksperiode", createOpprettVedtaksperiodePayload(data)).finally(() =>
      setIsFetching(false),
    );
  };

    const postArbeidsgiverSvarer = async (payload: ArbeidsgiverSvarerPayload): Promise<Response> => {
        return post("/vedtaksperiode", payload).finally(() =>
            setIsFetching(false),
        );
    };

  const onSubmit = async (data: Record<string, any>) => {
    setIsFetching(true);
    const response = await postOpprettVedtaksperiode(data);
    const { status } = response;
    setStatus(status);
    const errorBody = await response.text();
    setErrorBody(errorBody);

    if (status < 400) {
      addMessage({
        id: nanoid(),
        text: "Dokumenter er sendt.",
        timeToLiveMs: 4000,
      });
      subscribe(data.fnr, async (vedtaksperiodeId: string) => {
          console.log(`Jeg er en forespørselcallback for vedtaksperiodeId ${vedtaksperiodeId}`);
          const payload = createArbeidsgiverSvarerPayload(data, vedtaksperiodeId)
          if (payload == undefined) return
          setIsFetching(true);
          const response = await postArbeidsgiverSvarer(payload);
          const { status } = response;
          setStatus(status);
          const errorBody = await response.text();
          setErrorBody(errorBody);
          if (status < 400) {
              addMessage({
                  id: nanoid(),
                  text: "Arbeidsgiveropplysninger er sendt.",
                  timeToLiveMs: 4000,
              });
          }
      });
    }
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <div className={styles.OpprettDokumenter}>
          <div className={styles.DocumentContainer}>
            <PersonCard setErArbeidstaker={setErArbeidstaker} />
            {skalSendeSøknad && <SøknadCard />}
            {erArbeidstaker && <InntektsmeldingCard />}
            <DiverseCard />
          </div>
          {erArbeidstaker && (
            <>
              <Arbeidsgiverperioder />
              <EndringRefusjon />
            </>
          )}
          {skalSendeSøknad && <Ferieperioder />}
          {skalSendeSøknad && <Egenmeldingsdager />}
          {skalSendeSøknad && <InntektFraNyttArbeidsforhold />}
          <div className={styles.Flex}>
            <FetchButton status={status} isFetching={isFetching} type="submit">
              Opprett dokumenter
            </FetchButton>
            {typeof status === "number" && status >= 400 && (
              <ErrorMessage>
                Noe gikk galt! Melding fra server: {errorBody}, statuskode{" "}
                {status}
              </ErrorMessage>
            )}
          </div>
        </div>
      </form>
    </FormProvider>
  );
});
