import styles from "./OpprettDokumenter.module.css";
import addDays from "date-fns/addDays";
import format from "date-fns/format";
import { Card } from "../../components/Card";
import { Checkbox } from "../../components/Checkbox";
import { FormInput } from "../../components/FormInput";
import React, { useEffect } from "react";
import { useFormContext } from "react-hook-form";
import { validateArbeidsgrad, validateSykdomsgrad } from "../formValidation";
import {endOfMonth, subMonths} from "date-fns";

const formatDateString = (date: Date): string => format(date, "yyyy-MM-dd");

const nextDay = (date: Date): Date => addDays(date, 1);

const useUnregisterSøknadCard = () => {
  const { unregister } = useFormContext();

  useEffect(() => {
    return () => {
      unregister("sendtNav");
      unregister("sendtArbeidsgiver");
      unregister("faktiskgrad");
      unregister("harAndreInntektskilder");
      unregister("sykmeldingsgradSøknad");
    };
  }, []);
};

export const SøknadCard = React.memo(() => {
  const { watch, register, unregister, formState } = useFormContext();

  useUnregisterSøknadCard();

  const sykdomTom = watch("sykdomTom");
  const skalSendeSykmelding = watch("skalSendeSykmelding");

  useEffect(() => {
    if (skalSendeSykmelding) {
      unregister("sykmeldingsgradSøknad");
    }
  }, [skalSendeSykmelding]);

  const defaultDate = format(addDays(endOfMonth(subMonths(new Date(), 3)), 1), "yyyy-MM-dd")

  return (
    <Card>
      <h2 className={styles.Title}>Søknad</h2>
      <div className={styles.CardContainer}>
        <FormInput
          data-testid="sendtNav"
          label="Søknad sendt Nav"
          type="date"
          errors={formState.errors}
          defaultValue={
            sykdomTom
              ? formatDateString(nextDay(new Date(sykdomTom)))
              : defaultDate
          }
          {...register("sendtNav")}
        />
        <FormInput
          label="Søknad sendt arbeidsgiver"
          type="date"
          errors={formState.errors}
          {...register("sendtArbeidsgiver")}
        />
        <FormInput
          data-testid="faktiskgrad"
          label="Faktisk arbeidsgrad"
          errors={formState.errors}
          {...register("faktiskgrad", {
            required: false,
            validate: validateArbeidsgrad,
          })}
        />
        <Checkbox
          data-testid="harAndreInntektskilder"
          label="Har andre inntektskilder"
          errors={formState.errors}
          {...register("harAndreInntektskilder")}
        />
        {!skalSendeSykmelding && (
          <FormInput
            label="Sykdomsgrad i sykmeldingen"
            errors={formState.errors}
            defaultValue={100}
            {...register("sykmeldingsgradSøknad", {
              required: "Sykmeldingsgrad må fylles ut",
              validate: validateSykdomsgrad,
            })}
          />
        )}
      </div>
    </Card>
  );
});
