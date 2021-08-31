import { Card } from "../../components/Card";
import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { useFormContext } from "react-hook-form";
import React, { useEffect } from "react";
import { validateSykdomsgrad } from "../formValidation";

const useUnregisterSykmeldingCard = () => {
  const { unregister } = useFormContext();

  useEffect(() => {
    return () => {
      unregister("sykmeldingsgrad");
    };
  }, []);
};

export const SykmeldingCard = React.memo(() => {
  const { register, formState } = useFormContext();

  useUnregisterSykmeldingCard();

  return (
    <Card>
      <h2 className={styles.Title}>Sykmelding</h2>
      <div className={styles.CardContainer}>
        <FormInput
          label="Sykdomsgrad"
          errors={formState.errors}
          defaultValue={100}
          {...register("sykmeldingsgrad", {
            required: "Sykmeldingsgrad må angis",
            validate: validateSykdomsgrad,
          })}
        />
      </div>
    </Card>
  );
});
