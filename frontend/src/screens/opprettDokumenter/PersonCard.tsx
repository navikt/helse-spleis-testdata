import type { Component } from "solid-js";
import { createMemo, Show } from "solid-js";
import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { Checkbox } from "../../components/Checkbox";
import { Card } from "../../components/Card";
import { useFormContext } from "../../state/useFormContext";
import { invalidFnr, invalidOrganisasjonsnummer } from "./validators";
import { ErrorMessage } from "../../components/ErrorMessage";

export const PersonCard: Component = () => {
  const { register, errors, values } = useFormContext();

  const sendDocumentError = createMemo(() => !values().skalSendeSykmelding && !values().skalSendeSøknad && !values().skalSendeInntektsmelding && "Huk av for å sende minst ett dokument");

  return (
    <Card>
      <h2 class={styles.Title}>Person</h2>
      <div class={styles.CardContainer}>
        <FormInput
          register={register}
          errors={errors}
          label="Fødselsnummer"
          name="fnr"
          id="fnr"
          validation={invalidFnr}
          required
        />
        <FormInput
          register={register}
          errors={errors}
          label="Organisasjonsnummer"
          name="orgnummer"
          id="orgnummer"
          validation={invalidOrganisasjonsnummer}
          required
        />
        <FormInput
          register={register}
          errors={errors}
          label="Sykdom f.o.m."
          name="sykdomFom"
          id="sykdomFom"
          type="date"
          required
          defaultValue="2020-01-01"
        />
        <FormInput
          register={register}
          errors={errors}
          label="Sykdom t.o.m."
          name="sykdomTom"
          id="sykdomTom"
          type="date"
          required
          defaultValue="2020-01-31"
        />
        <Checkbox
          id="slettPerson"
          name="slettPerson"
          label="Slett og gjenskap data for personen"
          register={register}
          errors={errors}
        />
        <Checkbox
          id="skalSendeSykmelding"
          name="skalSendeSykmelding"
          label="Send sykmelding"
          register={register}
          defaultValue={true}
          aria-invalid={Boolean(sendDocumentError())}
        />
        <Checkbox
          id="skalSendeSøknad"
          name="skalSendeSøknad"
          label="Send søknad"
          register={register}
          defaultValue={true}
          aria-invalid={Boolean(sendDocumentError())}
        />
        <Checkbox
          id="skalSendeInntektsmelding"
          name="skalSendeInntektsmelding"
          label="Send inntektsmelding"
          register={register}
          defaultValue={true}
          aria-invalid={Boolean(sendDocumentError())}
        />
        <Show when={sendDocumentError()}>
          <ErrorMessage class={styles.DocumentError}>{sendDocumentError()}</ErrorMessage>
        </Show>
      </div>
    </Card>
  );
};
