import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { useFormContext } from "react-hook-form";
import React from "react";
import { validateSykdomsgrad } from "../formValidation";

export const Sykmelding = React.memo(() => {
  const { register, formState } = useFormContext();

  return (
    <div className={styles.CardContainer}>
      <FormInput
        label="Sykdomsgrad i sykmeldingen"
        errors={formState.errors}
        defaultValue={100}
        {...register("sykmeldingsgrad", {
          required: "Sykmeldingsgrad må angis",
          validate: validateSykdomsgrad,
        })}
      />
    </div>
  );
});
