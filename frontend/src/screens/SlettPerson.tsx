import styles from "./SlettPerson.module.css";
import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { FetchButton } from "../components/FetchButton";
import { del } from "../io/api";
import { ErrorMessage } from "../components/ErrorMessage";
import { useForm } from "react-hook-form";
import React, { useState } from "react";
import { validateFødselsnummer } from "./formValidation";

export const SlettPerson = React.memo(() => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setIsFetching(true);
    del("/person", { ident: data.fnr })
      .then((response) => setStatus(response.status))
      .catch((error) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className={styles.SlettPerson}>
        <Card>
          <h2 className={styles.Title}>Slett person</h2>
          <div className={styles.CardContainer}>
            <FormInput
              id="fnr"
              name="fnr"
              label="Fødselsnummer"
              errors={errors}
              {...register("fnr", {
                required: "Fødselsnummer må fylles ut",
                validate: validateFødselsnummer,
              })}
            />
            <FetchButton status={status} isFetching={isFetching}>
              Slett person
            </FetchButton>
            {status >= 400 && (
              <ErrorMessage>Kunne ikke slette person</ErrorMessage>
            )}
          </div>
        </Card>
      </div>
    </form>
  );
});
