import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";
import {validateInntekt, validateOrganisasjonsnummer} from "../formValidation";

type TilkommenId = string;

const formattedDateString = (date: Date): string =>
  date.toLocaleDateString("nb-NO", { dateStyle: "short" });

export const TilkommenInntekt = React.memo(() => {
  const {
    register,
    unregister,
    formState,
  } = useFormContext();

  const [tilkommen, setTilkommen] = useState<TilkommenId[]>([]);
  const { watch } = useFormContext();

  const tilkomneInntekter = watch("søknad.tilkomneInntekter");

  const addTilkommenInntekt = () => {
    setTilkommen((old) => [...old, nanoid()]);
  };

  const removeTilkommenInntekt = (index: number) => {
    unregister(`søknad.tilkomneInntekter`)
    setTilkommen((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addTilkommenInntekt} data-testid="tilkommenInntektButton">
        Legg til tilkommen inntekt
      </AddButton>
      {tilkommen.map((id, i) => (
        <Card key={id}>
          <div className={styles.CardContainer}>
            <div className={styles.PeriodContainer}>
              <FormInput
                data-testid={`startdato${i}`}
                type="date"
                label="Startdato for inntekt"
                errors={formState.errors}
                defaultValue={formattedDateString(new Date("2021-07-15"))}
                {...register(`søknad.tilkomneInntekter.${i}.datoFom`, {
                  required: "Startdato for inntekt må angis",
                })}
              />
              <FormInput
                data-testid={`sluttdato${i}`}
                type="date"
                label="Sluttdato for inntekt"
                errors={formState.errors}
                defaultValue={formattedDateString(new Date("2021-07-01"))}
                {...register(`søknad.tilkomneInntekter.${i}.datoTom`, {
                  validate: (value?: string): boolean | string => {
                    const startDato = tilkomneInntekter[i]['datoFom'] ?? "2021-07-01"
                    return value ? ((new Date(startDato) <= new Date(value)) || 'Sluttdato må være senere eller lik startdato') : true
                  },
                  required: false
                })}
              />
              <DeleteButton onClick={() => removeTilkommenInntekt(i)} />
            </div>
            <FormInput
                data-testid={`beløp${i}`}
                label="Beløp"
                errors={formState.errors}
                {...register(`søknad.tilkomneInntekter.${i}.beløp`, {
                  required: "Beløp må angis",
                  validate: validateInntekt,
                })}
            />
            <FormInput
                data-testid={`orgnummer${i}`}
                label="Organisasjonsnummer"
                errors={formState.errors}
                {...register(`søknad.tilkomneInntekter.${i}.orgnummer`, {
                  required: "orgnummer må angis",
                  validate: validateOrganisasjonsnummer,
                })}
            />
          </div>
        </Card>
      ))}
    </>
  );
});
