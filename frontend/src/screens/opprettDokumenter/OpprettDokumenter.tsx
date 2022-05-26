import styles from "./OpprettDokumenter.module.css";
import React, { useState } from "react";
import { FormProvider, useForm } from "react-hook-form";

import { del, post } from "../../io/api";
import { useSubscribe } from "../../io/subscription";

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
  EndringIRefusjonDto,
} from "../../io/api.d";

type Period = {
  fom: string;
  tom: string;
  id: string;
};

type OpprettVedtaksperiodePayload = PersonDTO &
  FellesDTO & {
    sykmelding?: SykmeldingDTO;
    søknad?: SøknadDTO;
    inntektsmelding?: InntektsmeldingDTO;
  };

const createPayload = (
  values: Record<string, any>
): OpprettVedtaksperiodePayload => {
  const valuesWithName = (values, name) =>
    Object.entries(values)
      .filter(([key]) => key.includes(name))
      .map((it) => it[1]);

  const mapPeriodArray = (values, k1, k2): any[] => {
    const first = valuesWithName(values, k1);
    const second = valuesWithName(values, k2);
    return first.map((it, i) => ({ fom: it, tom: second[i] })) ?? [];
  };

  const mapEndringIRefusjon = (values): EndringIRefusjonDto[] => {
    const endringsdato = valuesWithName(values, "endringsdato");
    const beløp = valuesWithName(values, "endringsbeløp");
    return endringsdato.map((it, i) => ({ endringsdato: it as string, beløp: beløp[i] as number })) ?? [];
  }

  const sykmelding = (): SykmeldingDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad,
  });

  const søknad = (): SøknadDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad ?? values.sykmeldingsgradSøknad,
    harAndreInntektskilder: values.harAndreInntektskilder ?? false,
    ferieperioder: mapPeriodArray(values, "ferieFom", "ferieTom"),
    faktiskgrad: values.faktiskgrad || undefined,
    sendtNav: values.sendtNav || undefined,
    sendtArbeidsgiver: values.sendtArbeidsgiver || undefined,
  });

  const inntektsmelding = (): InntektsmeldingDTO => ({
    inntekt: values.inntekt,
    refusjon: {
      opphørRefusjon: values.opphørRefusjon || null,
      refusjonsbeløp: values.refusjonsbeløp || null
    },
    ferieperioder: mapPeriodArray(values, "ferieFom", "ferieTom"),
    arbeidsgiverperiode: mapPeriodArray(values, "arbFom", "arbTom"),
    endringRefusjon: mapEndringIRefusjon(values),
    førsteFraværsdag: values.førsteFraværsdag,
  });

  return {
    fnr: values.fnr,
    orgnummer: values.orgnummer,
    sykdomFom: values.sykdomFom,
    sykdomTom: values.sykdomTom,
    sykmelding: values.skalSendeSykmelding ? sykmelding() : undefined,
    søknad: values.skalSendeSøknad ? søknad() : undefined,
    inntektsmelding: values.skalSendeInntektsmelding
      ? inntektsmelding()
      : undefined,
  };
};

export const OpprettDokumenter = React.memo(() => {
  const form = useForm({
    defaultValues: {
      slettPerson: false,
      skalSendeSykmelding: true,
      skalSendeSøknad: true,
      skalSendeInntektsmelding: true,
    },
  });

  const skalSlettePerson = form.watch("slettPerson");
  const skalSendeSykmelding = form.watch("skalSendeSykmelding");
  const skalSendeSøknad = form.watch("skalSendeSøknad");
  const skalSendeInntektsmelding = form.watch("skalSendeInntektsmelding");

  const [status, setStatus] = useState<number>();
  const [errorBody, setErrorBody] = useState<string>();
  const [isFetching, setIsFetching] = useState<boolean>(false);

  const [subscribe] = useSubscribe();

  const deletePerson = async (fødselsnummer: string): Promise<Response> => {
    setIsFetching(true);
    return del("/person", { ident: fødselsnummer })
      .catch((error) => {
        setStatus(error.status);
        return error;
      })
      .finally(() => setIsFetching(false));
  };

  const postPayload = async (data: Record<string, any>): Promise<Response> => {
    return post("/vedtaksperiode", createPayload(data)).finally(() =>
      setIsFetching(false)
    );
  };

  const onSubmit = async (data: Record<string, any>) => {

    if (skalSlettePerson) {
      const { status } = await deletePerson(data.fnr);
      if (status >= 400) return;
    }

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
          </div>
          {skalSendeInntektsmelding && (
            <>
              <Arbeidsgiverperioder />
              <EndringRefusjon />
            </>
          )}
          {(skalSendeInntektsmelding || skalSendeSøknad) && <Ferieperioder />}
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
