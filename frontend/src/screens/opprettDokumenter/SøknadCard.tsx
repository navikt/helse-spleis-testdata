import styles from "./OpprettDokumenter.module.css";
import addDays from "date-fns/addDays";
import format from "date-fns/format";
import {Card} from "../../components/Card";
import {Checkbox} from "../../components/Checkbox";
import {FormInput} from "../../components/FormInput";
import React from "react";
import {useFormContext} from "react-hook-form";
import {
  validateArbeidsgrad,
  validateInntekt,
  validateOptionalOrganisasjonsnummer,
  validateSykdomsgrad
} from "../formValidation";
import {endOfMonth, subMonths} from "date-fns";
import {ArbeidssituasjonDTO} from "../../utils/types";

const formatDateString = (date: Date): string => format(date, "yyyy-MM-dd");

const nextDay = (date: Date): Date => addDays(date, 1);

export const SøknadCard = React.memo(() => {
  const { watch, register, formState } = useFormContext();

  const sykdomFom = watch("sykdomFom");
  const sykdomTom = watch("sykdomTom");
  const skalSendeSykmelding = watch("skalSendeSykmelding");

  const arbeidssituasjon: ArbeidssituasjonDTO = watch("arbeidssituasjon")
  const skalViseTidligereArbeidsgiverOrgnummer = arbeidssituasjon == 'ARBEIDSLEDIG'

  const defaultDate = format(addDays(endOfMonth(subMonths(new Date(), 3)), 1), "yyyy-MM-dd")

  return (
    <Card>
      <h2 className={styles.Title}>Søknad</h2>
      <div className={styles.CardContainer}>
        {skalViseTidligereArbeidsgiverOrgnummer && <FormInput
            data-testid="tidligereArbeidsgiverOrgnummer"
            label="Tidligere arbeidsgiver sitt orgnummer"
            errors={formState.errors}
            {...register("søknad.tidligereArbeidsgiverOrgnummer", {
              validate: validateOptionalOrganisasjonsnummer,
              shouldUnregister: true
            })}
        />}
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
          {...register("søknad.sendtNav")}
        />
        <FormInput
          label="Søknad sendt arbeidsgiver"
          type="date"
          errors={formState.errors}
          {...register("søknad.sendtArbeidsgiver")}
        />
        <FormInput
          label="Arbeid gjenopptatt"
          type="date"
          errors={formState.errors}
          {...register("søknad.arbeidGjenopptatt", {
            required: false,
            validate: (value?: string): boolean | string =>
                value ? ((new Date(sykdomFom) <= new Date(value) && new Date(sykdomTom) >= new Date(value)) || 'Arbeid gjenopptatt kan ikke være eldre enn sykdomFom, eller nyere enn sykdomTom'): true,
          })}
        />
        <FormInput
          data-testid="faktiskgrad"
          label="Faktisk arbeidsgrad"
          errors={formState.errors}
          {...register("søknad.faktiskgrad", {
            required: false,
            validate: validateArbeidsgrad,
          })}
        />
        { arbeidssituasjon === 'SELVSTENDIG_NARINGSDRIVENDE' && <FormInput
            label="Årsinntekt fra Sigrun"
            errors={formState.errors}
                {...register("søknad.inntektFraSigrun", {
                  required: "Inntekt fra Sigrun må fylles ut",
                  validate: validateInntekt
                })}
            />
        }
        <Checkbox
          data-testid="harAndreInntektskilder"
          label="Har andre inntektskilder"
          errors={formState.errors}
          {...register("søknad.harAndreInntektskilder")}
        />
        {!skalSendeSykmelding && (
          <FormInput
            label="Sykdomsgrad i sykmeldingen"
            errors={formState.errors}
            defaultValue={100}
            {...register("søknad.sykmeldingsgrad", {
              required: "Sykmeldingsgrad må fylles ut",
              validate: validateSykdomsgrad,
              shouldUnregister: true
            })}
          />
        )}
      </div>
    </Card>
  );
});
