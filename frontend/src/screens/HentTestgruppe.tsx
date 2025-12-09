import React, { useState } from "react";
import { useForm } from "react-hook-form";

import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";

import { validateGruppeId } from "./formValidation";

import styles from "./HentTestgruppe.module.css";
import { FetchButton } from "../components/FetchButton";
import { get } from "../io/api";
import { ErrorMessage } from "../components/ErrorMessage";

interface DollyTestProps {}

export const HentTestgruppe: React.FC<DollyTestProps> = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setStatus(undefined);
    setIsFetching(true);
    get(`/gruppe/${data.gruppeId}`)
      .then((response) => setStatus(response.status))
      .catch((error) => setStatus(error.status ?? 404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className={styles.HentTestgruppe}>
        <Card>
          <h2 className={styles.Title}>Hent testgruppe</h2>
          <div className={styles.CardContainer}>
            <FormInput
              id="gruppeId"
              label="Gruppe-ID"
              errors={errors}
              {...register("gruppeId", {
                required: "Gruppe-ID må fylles ut",
                validate: validateGruppeId,
              })}
            />
            <FetchButton status={status} isFetching={isFetching}>
              Hent gruppe
            </FetchButton>
            {typeof status === "number" && status > 400 && (
              <ErrorMessage>
                Det skjedde en feil. Prøv igjen senere.
              </ErrorMessage>
            )}
          </div>
        </Card>
      </div>
    </form>
  );
};
