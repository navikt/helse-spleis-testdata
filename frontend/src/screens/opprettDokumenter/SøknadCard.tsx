import React from "react";
import { useFormContext } from "react-hook-form";
import { addDays, endOfMonth, format, subMonths } from "date-fns";
import { Heading, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { Checkbox } from "../../components/Checkbox";
import { FormInput } from "../../components/FormInput";
import { FormDatePicker } from "../../components/FormDatePicker";
import { FormSelect } from "../../components/FormSelect";
import {
  validateArbeidsgrad,
  validateInntekt,
  validateOptionalOrganisasjonsnummer,
  validateSykdomsgrad,
} from "../formValidation";
import { ArbeidssituasjonDTO } from "../../utils/types";

const formatDateString = (date: Date): string => format(date, "yyyy-MM-dd");

const nextDay = (date: Date): Date => addDays(date, 1);

export const SøknadCard = React.memo(() => {
  const { watch, register, formState, setValue } = useFormContext();

  const sykdomFom = watch("sykdomFom");
  const sykdomTom = watch("sykdomTom");
  const skalSendeSykmelding = watch("skalSendeSykmelding");

  const arbeidssituasjon: ArbeidssituasjonDTO = watch("arbeidssituasjon");
  const skalViseTidligereArbeidsgiverOrgnummer =
    arbeidssituasjon == "ARBEIDSLEDIG";
  const skalViseSelvstendigInputs =
    arbeidssituasjon === "SELVSTENDIG_NARINGSDRIVENDE" ||
    arbeidssituasjon === "BARNEPASSER" ||
    arbeidssituasjon == "FISKER" ||
    arbeidssituasjon === "JORDBRUKER";

  const defaultDate = format(
    addDays(endOfMonth(subMonths(new Date(), 3)), 1),
    "yyyy-MM-dd",
  );

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Søknad
        </Heading>
        {skalViseTidligereArbeidsgiverOrgnummer && (
          <FormInput
            data-testid="tidligereArbeidsgiverOrgnummer"
            label="Tidligere arbeidsgiver sitt orgnummer"
            errors={formState.errors}
            {...register("søknad.tidligereArbeidsgiverOrgnummer", {
              validate: validateOptionalOrganisasjonsnummer,
              shouldUnregister: true,
            })}
          />
        )}
        <FormDatePicker
          data-testid="sendtNav"
          label="Søknad sendt Nav"
          name="søknad.sendtNav"
          defaultValue={
            sykdomTom
              ? formatDateString(nextDay(new Date(sykdomTom)))
              : defaultDate
          }
        />
        <FormDatePicker
          label="Søknad sendt arbeidsgiver"
          name="søknad.sendtArbeidsgiver"
        />
        <FormDatePicker
          label="Arbeid gjenopptatt"
          name="søknad.arbeidGjenopptatt"
          rules={{
            required: false,
            validate: (value?: string): boolean | string =>
              value
                ? (new Date(sykdomFom) <= new Date(value) &&
                    new Date(sykdomTom) >= new Date(value)) ||
                  "Arbeid gjenopptatt kan ikke være eldre enn sykdomFom, eller nyere enn sykdomTom"
                : true,
          }}
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
        {skalViseSelvstendigInputs && (
          <>
            <FormInput
              label="Årsinntekt fra Sigrun"
              errors={formState.errors}
              {...register("søknad.inntektFraSigrun", {
                required: "Inntekt fra Sigrun må fylles ut",
                validate: validateInntekt,
              })}
            />
            <FormSelect
              label="Fravær før sykmeldingen"
              options={[{ value: "", label: "Ikke spurt om" }, "Ja", "Nei"]}
              defaultValue="Nei"
              {...register("søknad.fraværFørSykmeldingen")}
              onChange={(val) => {
                const verdi =
                  val.target.options[val.target.options.selectedIndex].value;
                setValue("søknad.fraværFørSykmeldingen", verdi);
              }}
            />
            <Checkbox
              data-testid="harBrukerOppgittForsikring"
              label="Har bruker oppgitt forsikring?"
              defaultChecked={false}
              errors={formState.errors}
              {...register("søknad.harBrukerOppgittForsikring")}
            />
            <FormDatePicker
              data-testid="meldingTilNavDagerFraSykmeldingFom"
              label="Melding til Nav dager fom"
              name="søknad.meldingTilNavDagerFraSykmeldingFom"
            />
            <FormDatePicker
              data-testid="meldingTilNavDagerFraSykmeldingTom"
              label="Melding til Nav dager tom"
              name="søknad.meldingTilNavDagerFraSykmeldingTom"
            />
          </>
        )}
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
              shouldUnregister: true,
            })}
          />
        )}
      </VStack>
    </Card>
  );
});
