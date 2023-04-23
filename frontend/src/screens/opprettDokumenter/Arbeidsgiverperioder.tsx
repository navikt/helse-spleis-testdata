import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";

type PeriodeId = string;

const formattedDateString = (date: Date): string =>
  date.toLocaleDateString("nb-NO", { dateStyle: "short" });

export const Arbeidsgiverperioder = React.memo(() => {
  const { register, unregister, formState } = useFormContext();
  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  const addArbeidsgiverperiode = () => {
    setPerioder((old) => [...old, nanoid()]);
  };

  const removeArbeidsgiverperiode = (index: number) => {
    unregister(`inntektsmelding.arbeidsgiverperiode`)
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton
        onClick={addArbeidsgiverperiode}
        data-testid="arbeidsgiverperioderButton"
      >
        Legg inn arbeidsgiverperioder
      </AddButton>
      {perioder.map((id, i) => (
        <Card key={id}>
          <div className={styles.PeriodContainer}>
            <FormInput
              data-testid={`arbeidsgiverFom${i}`}
              type="date"
              label="Arbeidsgiverperiode f.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2021-07-01"))}
              {...register(`inntektsmelding.arbeidsgiverperiode.${i}.fom`, {
                required: "Start av arbeidsgiverperioden må angis",
              })}
            />
            <FormInput
              data-testid={`arbeidsgiverTom${i}`}
              type="date"
              label="Arbeidsgiverperiode t.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2021-07-16"))}
              {...register(`inntektsmelding.arbeidsgiverperiode.${i}.tom`, {
                required: "Slutt av arbeidsgiverperioden må angis",
              })}
            />
            <DeleteButton onClick={() => removeArbeidsgiverperiode(i)} />
          </div>
        </Card>
      ))}
    </>
  );
});
