import styles from "./HentInntekt.module.css";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { get } from "../io/api";
import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { CopyField } from "../components/CopyField";
import { FetchButton } from "../components/FetchButton";
import { ErrorMessage } from "../components/ErrorMessage";
import { validateFødselsnummer } from "./formValidation";

export const HentInntekt = React.memo(() => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [inntekt, setInntekt] = useState<number>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setIsFetching(true);
    get("/person/inntekt", { ident: data.fnr })
      .then(async (response) => {
        const { beregnetMånedsinntekt } = await response.json();
        setInntekt(beregnetMånedsinntekt);
        setStatus(response.status);
      })
      .catch((_) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className={styles.HentInntekt}>
        <Card>
          <h2 className={styles.Title}>Hent inntekt</h2>
          <div className={styles.CardContainer}>
            <FormInput
              id="fnr"
              label="Fødselsnummer"
              errors={errors}
              {...register("fnr", {
                required: "Fødselsnummer må fylles ut",
                validate: validateFødselsnummer,
              })}
            />
            <FetchButton status={status} isFetching={isFetching}>
              Hent inntekt
            </FetchButton>
            {typeof status === "number" && status >= 400 && (
              <ErrorMessage>Kunne ikke hente inntekt</ErrorMessage>
            )}
          </div>
        </Card>
        <Card>
          <CopyField value={String(inntekt ?? "")} label="Inntekt" />
        </Card>
      </div>
    </form>
  );
});
