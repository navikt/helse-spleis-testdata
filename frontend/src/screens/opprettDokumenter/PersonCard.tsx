import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { Checkbox } from "../../components/Checkbox";
import { Card } from "../../components/Card";
import { ErrorMessage } from "../../components/ErrorMessage";
import { useFormContext } from "react-hook-form";
import React from "react";
import {
  validateFødselsnummer,
  validateOrganisasjonsnummer,
} from "../formValidation";

const useDocumentsValidator = () => {
  const { watch } = useFormContext();

  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const skalSendeSøknad = watch("skalSendeSøknad");
  const skalSendeInntektsmelding = watch("skalSendeInntektsmelding");

  return () =>
    skalSendeSykmelding ||
    skalSendeSøknad ||
    skalSendeInntektsmelding ||
    "Huk av for å sende minst ett dokument";
};

export const PersonCard = React.memo(() => {
  const { register, formState } = useFormContext();

  const validateSendsDocuments = useDocumentsValidator();

  return (
    <Card>
      <h2 className={styles.Title}>Person</h2>
      <div className={styles.CardContainer}>
        <FormInput
          data-testid="fnr"
          label="Fødselsnummer"
          errors={formState.errors}
          {...register("fnr", {
            required: "Fødselsnummer må fylles ut",
            validate: validateFødselsnummer,
          })}
        />
        <FormInput
          data-testid="orgnummer"
          label="Organisasjonsnummer"
          errors={formState.errors}
          {...register("orgnummer", {
            required: "Organisasjonsnummer må fylles ut",
            validate: validateOrganisasjonsnummer,
          })}
        />
        <FormInput
          label="Sykdom f.o.m."
          errors={formState.errors}
          type="date"
          defaultValue="2021-07-01"
          {...register("sykdomFom", {
            required: "Start av sykdomsforløp må angis",
          })}
        />
        <FormInput
          label="Sykdom t.o.m."
          errors={formState.errors}
          type="date"
          defaultValue="2021-07-31"
          {...register("sykdomTom", {
            required: "Slutt av sykdomsforløp må angis",
          })}
        />
        <Checkbox
          data-testid="slettPerson"
          label="Slett og gjenskap data for personen"
          {...register("slettPerson")}
        />
        <Checkbox
          label="Send sykmelding"
          {...register("skalSendeSykmelding", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        <Checkbox
          label="Send søknad"
          {...register("skalSendeSøknad", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        <Checkbox
          label="Send inntektsmelding"
          {...register("skalSendeInntektsmelding", {
            validate: validateSendsDocuments,
          })}
          aria-invalid={!!validateSendsDocuments()}
        />
        {validateSendsDocuments() && (
          <ErrorMessage className={styles.DocumentError}>
            {validateSendsDocuments()}
          </ErrorMessage>
        )}
      </div>
    </Card>
  );
});
