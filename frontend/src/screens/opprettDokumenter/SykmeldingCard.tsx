import type { Component } from "solid-js";
import { onCleanup } from "solid-js";
import { Card } from "../../components/Card";
import styles from "./OpprettDokumenter.module.css";
import { FormInput } from "../../components/FormInput";
import { useFormContext } from "../../state/useFormContext";
import { invalidSykdomsgrad } from "./validators";

export const SykmeldingCard: Component = () => {
  const { register, deregister, errors, values } = useFormContext();

  onCleanup(() => {
    deregister("sykmeldingsgrad");
  });

  return (
    <Card>
      <h2 class={styles.Title}>Sykmelding</h2>
      <div class={styles.CardContainer}>
        <FormInput
          register={register}
          errors={errors}
          label="Sykdomsgrad"
          name="sykmeldingsgrad"
          id="sykmeldingsgrad"
          required
          validation={invalidSykdomsgrad}
          defaultValue={100}
        />
      </div>
    </Card>
  );
};
