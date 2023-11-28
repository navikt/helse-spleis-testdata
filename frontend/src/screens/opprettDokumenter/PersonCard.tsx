import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { Checkbox } from "../../components/Checkbox";
import { Card } from "../../components/Card";
import { ErrorMessage } from "../../components/ErrorMessage";
import { useFormContext } from "react-hook-form";
import React, {useEffect, useState} from "react";
import {
  validateFødselsnummer,
  validateOrganisasjonsnummer,
} from "../formValidation";
import { SykdomTom } from "./SykdomTom";
import { SykdomFom } from "./SykdomFom";
import { DeleteButton } from "./DeleteButton";
import {ArbeidssituasjonDTO} from "../../utils/types";

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
  const { register, unregister, formState, watch } = useFormContext();
  const [deleteErrorMessage, setDeleteErrorMessage] = useState(undefined);

  const validateSendsDocuments = useDocumentsValidator();

  const arbeidssituasjon: ArbeidssituasjonDTO = watch("søknad.arbeidssituasjon")
  const [skalKreveOrgnummer, setSkalKreveOrgnummer] = useState(true)

  useEffect(() => {
    setSkalKreveOrgnummer(arbeidssituasjon === "ARBEIDSTAKER");
    if (arbeidssituasjon !== "ARBEIDSTAKER") {
      unregister("orgnummer")
    }
  }, [arbeidssituasjon]);


  const deleteFailed = (errorMessage: string) => {
    setDeleteErrorMessage(errorMessage);
  };

  return (
    <Card>
      <h2 className={styles.Title}>Person</h2>
      <div className={styles.CardContainer}>
        <span className={styles.Fødselsnummer}>
          <FormInput
            data-testid="fnr"
            label="Fødselsnummer"
            errors={formState.errors}
            {...register("fnr", {
              required: "Fødselsnummer må fylles ut",
              validate: validateFødselsnummer,
            })}
          />
          <DeleteButton errorCallback={deleteFailed} />
        </span>
        {skalKreveOrgnummer && <FormInput
          data-testid="orgnummer"
          label="Organisasjonsnummer"
          errors={formState.errors}
          {...register("orgnummer", {
            required: "Organisasjonsnummer må fylles ut",
            validate: validateOrganisasjonsnummer,
          })}
        />}
        <SykdomFom />
        <SykdomTom />
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
        {typeof validateSendsDocuments() === "string" && (
          <ErrorMessage className={styles.DocumentError}>
            {validateSendsDocuments()}
          </ErrorMessage>
        )}
        {/* Rendres alltid for å unngå resizing av card-et, som har width: max-content */}
        <ErrorMessage className={styles.DocumentError}>
          {deleteErrorMessage}
        </ErrorMessage>
      </div>
    </Card>
  );
});
