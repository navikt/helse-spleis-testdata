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

  const removeArbeidsgiverperiode = (id: PeriodeId) => {
    const index = perioder.findIndex((it) => it === id);
    unregister(`arbFom-${id}`);
    unregister(`arbTom-${id}`);
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addArbeidsgiverperiode}>
        Legg inn arbeidsgiverperioder
      </AddButton>
      {perioder.map((id) => (
        <Card key={id}>
          <div className={styles.PeriodContainer}>
            <FormInput
              type="date"
              label="Arbeidsgiverperiode f.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2020-01-01"))}
              {...register(`arbFom-${id}`, {
                required: "Start av arbeidsgiverperioden må angis",
              })}
            />
            <FormInput
              type="date"
              label="Arbeidsgiverperiode t.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2020-01-16"))}
              {...register(`arbTom-${id}`, {
                required: "Slutt av arbeidsgiverperioden må angis",
              })}
            />
            <DeleteButton onClick={() => removeArbeidsgiverperiode(id)} />
          </div>
        </Card>
      ))}
    </>
  );
});
