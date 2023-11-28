import styles from "./OpprettDokumenter.module.css";
import React, { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";

import { post } from "../../io/api";
import { useSubscribe } from "../../io/subscription";

import { DiverseCard } from "./DiverseCard";
import { PersonCard } from "./PersonCard";
import { SøknadCard } from "./SøknadCard";
import { Ferieperioder } from "./Ferieperioder";
import { SykmeldingCard } from "./SykmeldingCard";
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
import {Egenmeldingsdager} from "./Egenmeldingsdager";

type OpprettVedtaksperiodePayload = PersonDTO &
  FellesDTO & {
    sykmelding?: SykmeldingDTO;
    søknad?: SøknadDTO;
    inntektsmelding?: InntektsmeldingDTO;
    medlemskapAvklart: boolean;
  };

const createPayload = (
  values: Record<string, any>
): OpprettVedtaksperiodePayload => {
  const sykmelding = (): SykmeldingDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad,
  });

  const søknad = (): SøknadDTO => ({
    arbeidssituasjon: values.søknad.arbeidssituasjon,
    sykmeldingsgrad: values.sykmeldingsgrad ?? values.søknad.sykmeldingsgrad,
    harAndreInntektskilder: values.søknad.harAndreInntektskilder ?? false,
    ferieperioder: values.søknad.ferieperioder?.map(it => ({fom: it.fom, tom: it.tom})) ?? [],
    egenmeldingsdagerFraSykmelding: values.søknad.egenmeldingsdager,
    faktiskgrad: values.søknad.faktiskgrad || undefined,
    sendtNav: values.søknad.sendtNav || undefined,
    sendtArbeidsgiver: values.søknad.sendtArbeidsgiver || undefined,
    arbeidGjenopptatt: values.søknad.arbeidGjenopptatt || undefined,
  });

  const inntektsmelding = (): InntektsmeldingDTO => ({
    inntekt: values.inntektsmelding.inntekt,
    refusjon: {
      opphørRefusjon: values.inntektsmelding.opphørRefusjon || null,
      refusjonsbeløp: values.inntektsmelding.refusjonsbeløp || null
    },
    arbeidsgiverperiode: values.inntektsmelding.arbeidsgiverperiode?.map((it) => ({ fom: it.fom, tom: it.tom })) ?? [],
    endringRefusjon: values.inntektsmelding.endringIRefusjon?.map((it) => ({ endringsdato: it.endringsdato, beløp: it.endringsbeløp as number })) ?? [],
    førsteFraværsdag: values.inntektsmelding.førsteFraværsdag,
    begrunnelseForReduksjonEllerIkkeUtbetalt: values.inntektsmelding.begrunnelseForReduksjonEllerIkkeUtbetalt,
    harOpphørAvNaturalytelser: values.inntektsmelding.harOpphørAvNaturalytelser ?? false
  });

  return {
    fnr: values.fnr,
    orgnummer: values.orgnummer || null,
    sykdomFom: values.sykdomFom,
    sykdomTom: values.sykdomTom,
    sykmelding: values.skalSendeSykmelding ? sykmelding() : undefined,
    søknad: values.skalSendeSøknad ? søknad() : undefined,
    medlemskapAvklart: values.medlemskapAvklart,
    inntektsmelding: values.skalSendeInntektsmelding
      ? inntektsmelding()
      : undefined,
  };
};

export const OpprettDokumenter = React.memo(() => {
  const form = useForm({
    defaultValues: {
      skalSendeSykmelding: true,
      skalSendeSøknad: true,
      skalSendeInntektsmelding: true,
      medlemskapAvklart: true,
      skalKreveOrgnummer: true
    },
  });

  const skalSendeSykmelding = form.watch("skalSendeSykmelding");
  const skalSendeSøknad = form.watch("skalSendeSøknad");
  const skalSendeInntektsmelding = form.watch("skalSendeInntektsmelding");

  const [status, setStatus] = useState<number>();
  const [errorBody, setErrorBody] = useState<string>();
  const [isFetching, setIsFetching] = useState<boolean>(false);

  const [subscribe] = useSubscribe();

  const postPayload = async (data: Record<string, any>): Promise<Response> => {
    return post("/vedtaksperiode", createPayload(data)).finally(() =>
      setIsFetching(false)
    );
  };

  const onSubmit = async (data: Record<string, any>) => {
    setIsFetching(true);

    setTimeout(async () => {
      const response = await postPayload(data);
      const {status} = response
      setStatus(status);
      const errorBody = await response.text()
      setErrorBody(errorBody);

      if (status < 400) {
        subscribe(data.fnr);
      }
    }, 1000)
  };

  return (
    <FormProvider {...form}>
      <form onSubmit={form.handleSubmit(onSubmit)}>
        <div className={styles.OpprettDokumenter}>
          <div className={styles.DocumentContainer}>
            <PersonCard />
            {skalSendeSykmelding && <SykmeldingCard />}
            {skalSendeSøknad && <SøknadCard />}
            {skalSendeInntektsmelding && <InntektsmeldingCard />}
            <DiverseCard />
          </div>
          {skalSendeInntektsmelding && (
            <>
              <Arbeidsgiverperioder />
              <EndringRefusjon />
            </>
          )}
          {skalSendeSøknad && <Ferieperioder />}
          {skalSendeSøknad && <Egenmeldingsdager />}
          <div className={styles.Flex}>
            <FetchButton status={status} isFetching={isFetching} type="submit">
              Opprett dokumenter
            </FetchButton>
            {status >= 400 && (
              <ErrorMessage>
                Noe gikk galt! Melding fra server: {errorBody}, statuskode {status}
              </ErrorMessage>
            )}
          </div>
        </div>
      </form>
    </FormProvider>
  );
});
