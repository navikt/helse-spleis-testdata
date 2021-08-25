import styles from "./OpprettDokumenter.module.css";
import type { Component } from "solid-js";
import { createEffect, createSignal, onCleanup } from "solid-js";
import { useFormContext } from "../../state/useFormContext";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { invalidFnr, nonNumerical } from "./validators";
import { get } from "../../io/api";

const validInntekt = (value: string): false | string =>
  nonNumerical(value, "Inntekten må være numerisk");

export const InntektsmeldingCard: Component = (props) => {
  const { register, deregister, errors, values, setValue } = useFormContext();
  const [fetchedInntekt, setFetchedInntekt] = createSignal(false);

  onCleanup(() => {
    deregister("førstefraværsdag");
    deregister("inntekt");
  });

  createEffect(() => {
    const fødselsnummer = values().fnr;
    if (!invalidFnr(fødselsnummer) && !fetchedInntekt()) {
      setFetchedInntekt(true);
      get("/person/inntekt", { ident: fødselsnummer })
        .then(async (result) => {
          const response = await result.json();
          console.log(response);
          setValue("inntekt", response.beregnetMånedsinntekt);
        })
        .catch(error => console.log(error));
    }
  });

  return (
    <Card>
      <h2 class={styles.Title}>Inntektsmelding</h2>
      <div class={styles.CardContainer}>
        <FormInput
          register={register}
          errors={errors}
          label="Første fraværsdag"
          name="førstefraværsdag"
          id="førstefraværsdag"
          type="date"
          required
          defaultValue="2020-01-01"
        />
        <FormInput
          register={register}
          errors={errors}
          label="Inntekt"
          name="inntekt"
          id="inntekt"
          validation={validInntekt}
          required
        />
      </div>
    </Card>
  );
};
