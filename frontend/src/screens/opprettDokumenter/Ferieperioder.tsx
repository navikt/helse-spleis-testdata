import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import type { Component } from "solid-js";
import { createSignal, For } from "solid-js";
import { useFormContext } from "../../state/useFormContext";

export const Ferieperioder: Component = () => {
  const { register, deregister, errors } = useFormContext();
  const [feriePeriods, setFeriePeriods] = createSignal<string[]>([]);

  const addFerieperiode = () => {
    setFeriePeriods((old) => [...old, nanoid()]);
  };

  const removeFerieperiode = (id: string) => {
    const index = feriePeriods().findIndex((it) => it === id);
    deregister(`ferieFom-${id}`);
    deregister(`ferieTom-${id}`);
    setFeriePeriods((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addFerieperiode}>Legg inn ferieperioder</AddButton>
      <For each={feriePeriods()}>
        {(id: string, i) => (
          <Card>
            <div class={styles.PeriodContainer}>
              <FormInput
                register={register}
                errors={errors}
                label="Ferieperiode f.o.m."
                name={`ferieFom-${id}`}
                id={`ferieFom-${id}`}
                type="date"
                required
                defaultValue={new Date("2020-01-01").toLocaleDateString(
                  "nb-NO",
                  {
                    dateStyle: "short",
                  }
                )}
              />
              <FormInput
                register={register}
                errors={errors}
                label="Ferieperiode t.o.m."
                name={`ferieTom-${id}`}
                id={`ferieTom-${id}`}
                type="date"
                required
                defaultValue={new Date("2020-01-10").toLocaleDateString(
                  "nb-NO",
                  {
                    dateStyle: "short",
                  }
                )}
              />
              <DeleteButton onClick={() => removeFerieperiode(id)} />
            </div>
          </Card>
        )}
      </For>
    </>
  );
};
