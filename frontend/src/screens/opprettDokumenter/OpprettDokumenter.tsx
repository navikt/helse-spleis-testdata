import styles from "./OpprettDokumenter.module.css";
import type { FormValues } from "../../state/useForm";
import { Fade } from "../../components/Fade";
import { PersonCard } from "./PersonCard";
import { SykmeldingCard } from "./SykmeldingCard";
import { SøknadCard } from "./SøknadCard";
import { Arbeidsgiverperioder } from "./Arbeidsgiverperioder";
import { Ferieperioder } from "./Ferieperioder";
import { EndringRefusjon } from "./EndringRefusjon";
import { post } from "../../io/api";
import type { FellesDTO, InntektsmeldingDTO, PersonDTO, SykmeldingDTO, SøknadDTO } from "../../io/api.d";
import { useFormContext, withFormProvider } from "../../state/useFormContext";
import { createSignal, Match, Show, Switch } from "solid-js";
import { ErrorMessage } from "../../components/ErrorMessage";
import { FetchButton } from "../../components/FetchButton";
import { InntektsmeldingCard } from "./InntektsmeldingCard";

type Period = {
  fom: string;
  tom: string;
  id: string;
};

type OpprettVedtaksperiodePayload = PersonDTO & FellesDTO & {
  sykmelding?: SykmeldingDTO,
  søknad?: SøknadDTO,
  inntektsmelding?: InntektsmeldingDTO,
};

const createPayload = (values: FormValues): OpprettVedtaksperiodePayload => {
  const valuesWithName = (values, name) =>
    Object.entries(values)
      .filter(([key]) => key.includes(name))
      .map((it) => it[1]);

  const mapPeriodArray = (values, k1, k2): any[] => {
    const first = valuesWithName(values, k1);
    const second = valuesWithName(values, k2);
    return first.map((it, i) => ({ fom: it, tom: second[i] })) ?? [];
  };

  const sykmelding = (): SykmeldingDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad
  });

  const søknad = (): SøknadDTO => ({
    sykmeldingsgrad: values.sykmeldingsgrad,
    harAndreInntektskilder: values.harAndreInntektskilder ?? false,
    ferieperioder: mapPeriodArray(values, "ferieFom", "ferieTom"),
    faktiskgrad: values.faktiskgrad || undefined,
    sendtNav: values.sendtNav || undefined,
    sendtArbeidsgiver: values.sendtArbeidsgiver || undefined
  });

  const inntektsmelding = (): InntektsmeldingDTO => ({
    inntekt: values.inntekt,
    ferieperioder: mapPeriodArray(values, "ferieFom", "ferieTom"),
    arbeidsgiverperiode: mapPeriodArray(values, "arbFom", "arbTom"),
    endringRefusjon: values.endringRefusjon,
    opphørRefusjon: values.opphørRefusjon,
    førsteFraværsdag: values.førsteFraværsdag
  });

  return {
    fnr: values.fnr,
    orgnummer: values.orgnummer,
    sykdomFom: values.sykdomFom,
    sykdomTom: values.sykdomTom,
    sykmelding: values.skalSendeSykmelding ? sykmelding() : undefined,
    søknad: values.skalSendeSøknad ? søknad() : undefined,
    inntektsmelding: values.skalSendeInntektsmelding ? inntektsmelding() : undefined
  };
};

export const OpprettDokumenter = withFormProvider(() => {
  const { register, deregister, errors, values } = useFormContext();
  const [status, setStatus] = createSignal<number>();
  const [isFetching, setIsFetching] = createSignal<boolean>(false);

  const onSubmit = async (event: Event) => {
    event.preventDefault();
    setIsFetching(true);

    const { status } = await post("/vedtaksperiode", createPayload(values()));
    setStatus(status);
  };

  return (
    <Fade>
      <form onSubmit={onSubmit}>
        <div class={styles.OpprettDokumenter}>
          <div class={styles.DocumentContainer}>
            <PersonCard />
            <Show when={values().skalSendeSykmelding}>
              <SykmeldingCard />
            </Show>
            <Show when={values().skalSendeSøknad}>
              <SøknadCard />
            </Show>
            <Show when={values().skalSendeInntektsmelding}>
              <InntektsmeldingCard />
            </Show>
          </div>
          <Show when={values().skalSendeInntektsmelding}>
            <Arbeidsgiverperioder />
            <EndringRefusjon />
          </Show>
          <Show when={values().skalSendeInntektsmelding || values().skalSendeSøknad}>
            <Ferieperioder />
          </Show>
          <div class={styles.Flex}>
            <FetchButton
              status={status()}
              isFetching={isFetching()}
              type="submit"
            >
              Opprett dokumenter
            </FetchButton>
            <Switch>
              <Match when={status() >= 400}>
                <ErrorMessage>
                  Noe gikk galt! Mottok respons med statuskode {status()}
                </ErrorMessage>
              </Match>
              <Match when={status() < 400}>
                <p>Dokumentene er sendt!</p>
              </Match>
            </Switch>
          </div>
        </div>
      </form>
    </Fade>
  );
});
