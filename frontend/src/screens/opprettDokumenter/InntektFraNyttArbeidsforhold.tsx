import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";
import {
  validateOptionalInntekt,
  validateOrganisasjonsnummer,
} from "../formValidation";

type InntektFraNyttArbeidsforholdId = string;

export const InntektFraNyttArbeidsforhold = React.memo(() => {
  const { register, unregister, formState } = useFormContext();

  const [inntektFraNyttArbeidsforhold, setInntektFraNyttArbeidsforhold] =
    useState<InntektFraNyttArbeidsforholdId[]>([]);
  const { watch } = useFormContext();

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
          <div className={styles.CardContainer}>
            <div className={styles.PeriodContainer}>
              <FormInput
                data-testid={`startdato${i}`}
                type="date"
                label="Startdato for inntekt"
                errors={formState.errors}
                defaultValue={defaultFom}
                {...register(
                  `søknad.inntektFraNyttArbeidsforhold.${i}.datoFom`,
                  {
                    required: "Startdato for inntekt må angis",
                  },
                )}
              />
              <FormInput
                data-testid={`sluttdato${i}`}
                type="date"
                label="Sluttdato for inntekt"
                errors={formState.errors}
                defaultValue={defaultTom}
                {...register(
                  `søknad.inntektFraNyttArbeidsforhold.${i}.datoTom`,
                  {
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
                  },
                )}
              />
              <DeleteButton
                onClick={() => removeInntektFraNyttArbeidsforhold(i)}
              />
            </div>
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
          </div>
        </Card>
      ))}
    </>
  );
});
