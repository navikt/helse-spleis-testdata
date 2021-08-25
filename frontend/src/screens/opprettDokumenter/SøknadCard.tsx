import type { Component } from "solid-js";
import { createEffect, onCleanup, Show } from "solid-js";
import styles from "./OpprettDokumenter.module.css";
import addDays from "date-fns/addDays";
import format from "date-fns/format";
import { Card } from "../../components/Card";
import { Checkbox } from "../../components/Checkbox";
import { FormInput } from "../../components/FormInput";
import { useFormContext } from "../../state/useFormContext";
import { invalidArbeidsgrad, invalidSykdomsgrad } from "./validators";

const formatDateString = (date: Date): string => format(date, "yyyy-MM-dd");

const nextDay = (date: Date): Date => addDays(date, 1);

export const SøknadCard: Component = () => {
  const { register, deregister, errors, values } = useFormContext();

  createEffect(() => {
    if (values().skalSendeSykmelding) {
      deregister("sykmeldingsgradSøknad");
    }
  });

  onCleanup(() => {
    deregister("sykmeldingsgradSøknad");
    deregister("sendtNav");
    deregister("sendtArbeidsgiver");
    deregister("faktiskgrad");
    deregister("harAndreInntektskilder");
  });

  return (
    <Card>
      <h2 class={styles.Title}>Søknad</h2>
      <div class={styles.CardContainer}>
        <FormInput
          register={register}
          errors={errors}
          label="Søknad sendt Nav"
          name="sendtNav"
          id="sendtNav"
          type="date"
          defaultValue={values().sykdomTom ? formatDateString(nextDay(new Date(values().sykdomTom))) : "2020-02-01"}
        />
        <FormInput
          register={register}
          errors={errors}
          label="Søknad sendt arbeidsgiver"
          name="sendtArbeidsgiver"
          id="sendtArbeidsgiver"
          type="date"
        />
        <FormInput
          register={register}
          errors={errors}
          label="Faktisk arbeidsgrad"
          name="faktiskgrad"
          id="faktiskgrad"
          validation={invalidArbeidsgrad}
        />
        <Checkbox
          id="harAndreInntektskilder"
          name="harAndreInntektskilder"
          label="Har andre inntektskilder"
          register={register}
          errors={errors}
        />
        <Show when={!values().skalSendeSykmelding}>
          <FormInput
            register={register}
            errors={errors}
            label="Sykdomsgrad i sykmeldingen"
            name="sykmeldingsgradSøknad"
            id="sykmeldingsgradSøknad"
            required
            validation={invalidSykdomsgrad}
            defaultValue={100}
          />
        </Show>
      </div>
    </Card>
  );
};
