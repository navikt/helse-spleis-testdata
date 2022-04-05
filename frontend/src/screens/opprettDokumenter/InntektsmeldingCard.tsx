import styles from "./OpprettDokumenter.module.css";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { get } from "../../io/api";
import React, { useEffect, useState } from "react";
import { useFormContext } from "react-hook-form";
import {validateFødselsnummer, validateInntekt, validateRefusjonsbeløp} from "../formValidation";

const useUnregisterInntektsmeldingCard = () => {
  const { unregister } = useFormContext();
  useEffect(() => {
    return () => {
      unregister("førsteFraværsdag");
      unregister("inntekt");
    };
  }, []);
};

const useFetchInntekt = () => {
  const { watch, setValue, clearErrors } = useFormContext();
  const fødselsnummer = watch("fnr");
  const [fetchedInntekt, setFetchedInntekt] = useState(false);

  useEffect(() => {
    if (!fetchedInntekt && validateFødselsnummer(fødselsnummer) === true) {
      setFetchedInntekt(true);
      get("/person/inntekt", { ident: fødselsnummer })
        .then(async (result) => {
          const response = await result.json();
          clearErrors("inntekt");
          clearErrors("refusjonsbeløp");
          setValue("inntekt", String(response.beregnetMånedsinntekt));
          setValue("refusjonsbeløp", String(response.beregnetMånedsinntekt));
        })
        .catch((error) => console.log(error));
    }
  }, [fetchedInntekt, fødselsnummer]);
};

export const InntektsmeldingCard = React.memo(() => {
  const { register, formState } = useFormContext();

  useUnregisterInntektsmeldingCard();
  useFetchInntekt();

  return (
    <Card>
      <h2 className={styles.Title}>Inntektsmelding</h2>
      <div className={styles.CardContainer}>
        <FormInput
          data-testid="førsteFraværsdag"
          type="date"
          label="Første fraværsdag"
          errors={formState.errors}
          defaultValue="2021-07-01"
          {...register("førsteFraværsdag", {
            required: "Første fraværsdag må angis",
          })}
        />
        <FormInput
          data-testid="opphørRefusjon"
          type="date"
          label="Siste dag med refusjon"
          errors={formState.errors}
          {...register("opphørRefusjon")}
        />
        <FormInput
          data-testid="inntekt"
          label="Inntekt"
          errors={formState.errors}
          {...register("inntekt", {
            required: "Inntekt må angis",
            validate: validateInntekt,
          })}
        />
        <FormInput
          data-testid="refusjonsbeløp"
          label="Refusjonsbeløp"
          errors={formState.errors}
          {...register("refusjonsbeløp", {
            validate: validateRefusjonsbeløp,
          })}
        />
      </div>
    </Card>
  );
});
