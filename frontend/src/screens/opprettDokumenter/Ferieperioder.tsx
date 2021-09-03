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

export const Ferieperioder = React.memo(() => {
  const { register, unregister, formState } = useFormContext();

  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  const addFerieperiode = () => {
    setPerioder((old) => [...old, nanoid()]);
  };

  const removeFerieperiode = (id: PeriodeId) => {
    const index = perioder.findIndex((it) => it === id);
    unregister(`ferieFom-${id}`);
    unregister(`ferieTom-${id}`);
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addFerieperiode} data-testid="ferieButton">
        Legg inn ferieperioder
      </AddButton>
      {perioder.map((id, i) => (
        <Card>
          <div className={styles.PeriodContainer}>
            <FormInput
              data-testid={`ferieFom${i}`}
              type="date"
              label="Ferieperiode f.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2021-07-01"))}
              {...register(`ferieFom-${id}`, {
                required: "Start av ferieperioden må angis",
              })}
            />
            <FormInput
              data-testid={`ferieTom${i}`}
              type="date"
              label="Ferieperiode t.o.m."
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2021-07-10"))}
              {...register(`ferieTom-${id}`, {
                required: "Slutt av ferieperioden må angis",
              })}
            />
            <DeleteButton onClick={() => removeFerieperiode(id)} />
          </div>
        </Card>
      ))}
    </>
  );
});
