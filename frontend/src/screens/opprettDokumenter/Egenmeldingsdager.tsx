import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";

type DagId = string;

const formattedDateString = (date: Date): string =>
  date.toLocaleDateString("nb-NO", { dateStyle: "short" });

export const Egenmeldingsdager = React.memo(() => {
  const { register, unregister, formState } = useFormContext();

  const [dager, setDager] = useState<DagId[]>([]);

  const addEgenmeldingsdager = () => {
    setDager((old) => [...old, nanoid()]);
  };

  const removeEgenmeldingsdager = (index: number) => {
    unregister(`søknad.egenmeldingsdager`);
    setDager((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addEgenmeldingsdager} data-testid="egenmeldingsButton">
        Legg inn egenmeldingsdager
      </AddButton>
      {dager.map((id, i) => (
        <Card key={id}>
          <div className={styles.PeriodContainer}>
            <FormInput
              data-testid={`egenmeldingsdag${i}`}
              type="date"
              label="Egenmeldingsdag"
              errors={formState.errors}
              defaultValue={formattedDateString(new Date("2021-07-01"))}
              {...register(`søknad.egenmeldingsdager.${i}`, {
                required: "Dato for egenmelding må angis",
              })}
            />
            <DeleteButton onClick={() => removeEgenmeldingsdager(i)} />
          </div>
        </Card>
      ))}
    </>
  );
});
