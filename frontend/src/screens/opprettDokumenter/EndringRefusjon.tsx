import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import React, { useState } from "react";
import { useFormContext } from "react-hook-form";
import { validateInntekt } from "../formValidation";

type OpphørId = string;

const formattedDateString = (date: Date): string =>
  date.toLocaleDateString("nb-NO", { dateStyle: "short" });

export const EndringRefusjon = React.memo(() => {
  const {
    register,
    unregister,
    formState,
  } = useFormContext();

  const [opphør, setOpphør] = useState<OpphørId[]>([]);

  const addEndring = () => {
    setOpphør((old) => [...old, nanoid()]);
  };

  const removeEndring = (id: OpphørId) => {
    const index = opphør.findIndex((it) => it === id);
    unregister(`endringsdato-${id}`);
    unregister(`endringsbeløp-${id}`);
    setOpphør((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addEndring} data-testid="endringButton">
        Legg inn endring i refusjon
      </AddButton>
      {opphør.map((id, i) => (
        <Card key={id}>
          <div className={styles.CardContainer}>
            <div className={styles.PeriodContainer}>
              <FormInput
                data-testid={`endringsdato${i}`}
                type="date"
                label="Dato for endring"
                errors={formState.errors}
                defaultValue={formattedDateString(new Date("2021-07-01"))}
                {...register(`endringsdato-${id}`, {
                  required: "Dato for endring må angis",
                })}
              />
              <DeleteButton onClick={() => removeEndring(id)} />
            </div>
            <FormInput
                data-testid={`endringsbeløp${i}`}
                label="Beløp for endring"
                errors={formState.errors}
                {...register(`endringsbeløp-${id}`, {
                  required: "Beløp for endring må angis",
                  validate: validateInntekt,
                })}
            />
          </div>
        </Card>
      ))}
    </>
  );
});
