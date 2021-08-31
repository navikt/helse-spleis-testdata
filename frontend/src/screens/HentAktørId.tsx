import styles from "./HentAktørId.module.css";
import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { FetchButton } from "../components/FetchButton";
import { CopyField } from "../components/CopyField";
import { ErrorMessage } from "../components/ErrorMessage";
import { get } from "../io/api";
import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { validateFødselsnummer } from "./formValidation";

export const HentAktørId = React.memo(() => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [aktørId, setAktørId] = useState<string>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setIsFetching(true);
    get("/person/aktorid", { ident: data.fnr })
      .then(async (response) => {
        const aktørId = await response.text();
        setAktørId(aktørId);
        setStatus(response.status);
      })
      .catch((error) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <div className={styles.HentAktørId}>
        <Card>
          <h2 className={styles.Title}>Hent aktør-ID</h2>
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
              Hent aktør-ID
            </FetchButton>
            {status >= 400 && (
              <ErrorMessage>Kunne ikke hente aktør-ID</ErrorMessage>
            )}
          </div>
        </Card>
        <Card>
          <CopyField value={String(aktørId ?? "")} label="Aktør-ID" />
        </Card>
      </div>
    </form>
  );
});
