import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";

type OpphørId = string;

const formattedDateString = (date: Date): string =>
  date.toLocaleDateString("nb-NO", { dateStyle: "short" });

export const EndringRefusjon = React.memo(() => {
  const {
    register,
    unregister,
    formState: { errors },
  } = useFormContext();

  const [opphør, setOpphør] = useState<OpphørId[]>([]);

  const addEndring = () => {
    setOpphør((old) => [...old, nanoid()]);
  };

  const removeEndring = (id: OpphørId) => {
    const index = opphør.findIndex((it) => it === id);
    unregister(`endring${id}`);
    setOpphør((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addEndring} data-testid="endringButton">
        Legg inn endring i refusjon
      </AddButton>
      {opphør.map((id, i) => (
        <Card key={id}>
          <div className={styles.PeriodContainer}>
            <FormInput
              data-testid={`endring${i}`}
              type="date"
              label="Dato for endring"
              errors={errors}
              defaultValue={formattedDateString(new Date("2021-07-01"))}
              {...register(`endring${id}`, {
                required: "Dato for endring må angis",
              })}
            />
            <DeleteButton onClick={() => removeEndring(id)} />
          </div>
        </Card>
      ))}
    </>
  );
});
