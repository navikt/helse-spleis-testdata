import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import {
  validateOptionalInntekt,
  validateOrganisasjonsnummer,
} from "../formValidation";

type InntektFraNyttArbeidsforholdId = string;

export const InntektFraNyttArbeidsforhold = React.memo(() => {
  const { register, unregister, formState, watch } = useFormContext();

  const [inntektFraNyttArbeidsforhold, setInntektFraNyttArbeidsforhold] =
    useState<InntektFraNyttArbeidsforholdId[]>([]);

  const inntekterFraNyeArbeidsforhold = watch(
    "søknad.inntektFraNyttArbeidsforhold",
  );
  const defaultFom = watch("sykdomFom");
  const defaultTom = watch("sykdomTom");

  const addInntektFraNyttArbeidsforhold = () => {
    setInntektFraNyttArbeidsforhold((old) => [...old, nanoid()]);
  };

  const removeInntektFraNyttArbeidsforhold = (index: number) => {
    unregister(`søknad.inntektFraNyttArbeidsforhold`);
    setInntektFraNyttArbeidsforhold((old) => [
      ...old.slice(0, index),
      ...old.slice(index + 1),
    ]);
  };

  return (
    <>
      <AddButton
        onClick={addInntektFraNyttArbeidsforhold}
        data-testid="inntektFraNyttArbeidsforholdButton"
      >
        Legg til inntekt fra nytt arbeidsforhold
      </AddButton>
      {inntektFraNyttArbeidsforhold.map((id, i) => (
        <Card key={id}>
          <VStack gap="space-16">
            <HStack gap="space-16" align="end" wrap={false}>
              <FormDatePicker
                data-testid={`startdato${i}`}
                label="Startdato for inntekt"
                name={`søknad.inntektFraNyttArbeidsforhold.${i}.datoFom`}
                defaultValue={defaultFom}
                rules={{ required: "Startdato for inntekt må angis" }}
              />
              <FormDatePicker
                data-testid={`sluttdato${i}`}
                label="Sluttdato for inntekt"
                name={`søknad.inntektFraNyttArbeidsforhold.${i}.datoTom`}
                defaultValue={defaultTom}
                rules={{
                  validate: (value?: string): boolean | string => {
                    const startDato =
                      inntekterFraNyeArbeidsforhold[i]["datoFom"] ??
                      "2021-07-01";
                    return value
                      ? new Date(startDato) <= new Date(value) ||
                          "Sluttdato må være senere eller lik startdato"
                      : true;
                  },
                  required: "Startdato for inntekt må angis",
                }}
              />
              <DeleteButton
                aria-label="Fjern inntekt fra nytt arbeidsforhold"
                onClick={() => removeInntektFraNyttArbeidsforhold(i)}
              />
            </HStack>
            <FormInput
              data-testid={`beløp${i}`}
              label="Beløp"
              errors={formState.errors}
              {...register(`søknad.inntektFraNyttArbeidsforhold.${i}.belop`, {
                required: "Beløp må angis",
                validate: validateOptionalInntekt,
              })}
            />
            <FormInput
              data-testid={`orgnummer${i}`}
              label="Organisasjonsnummer"
              errors={formState.errors}
              {...register(
                `søknad.inntektFraNyttArbeidsforhold.${i}.arbeidsstedOrgnummer`,
                {
                  required: "orgnummer må angis",
                  validate: validateOrganisasjonsnummer,
                },
              )}
            />
          </VStack>
        </Card>
      ))}
    </>
  );
});
