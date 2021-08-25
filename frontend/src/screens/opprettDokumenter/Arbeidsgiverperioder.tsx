import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import type { Component } from "solid-js";
import { createSignal, For } from "solid-js";
import { useFormContext } from "../../state/useFormContext";

export const Arbeidsgiverperioder: Component = () => {
  const { register, deregister, errors } = useFormContext();
  const [arbPeriods, setArbPeriods] = createSignal<string[]>([]);

  const addArbeidsgiverperiode = () => {
    setArbPeriods((old) => [...old, nanoid()]);
  };

  const removeArbeidsgiverperiode = (id: string) => {
    const index = arbPeriods().findIndex((it) => it === id);
    deregister(`arbFom-${id}`);
    deregister(`arbTom-${id}`);
    setArbPeriods((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addArbeidsgiverperiode}>
        Legg inn arbeidsgiverperioder
      </AddButton>
      <For each={arbPeriods()}>
        {(id: string) => (
          <Card>
            <div class={styles.PeriodContainer}>
              <FormInput
                register={register}
                errors={errors}
                label="Arbeidsgiverperiode f.o.m."
                name={`arbFom-${id}`}
                id={`arbFom-${id}`}
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
                label="Arbeidsgiverperiode t.o.m."
                name={`arbTom-${id}`}
                id={`arbTom-${id}`}
                type="date"
                required
                defaultValue={new Date("2020-01-16").toLocaleDateString(
                  "no-NB",
                  {
                    dateStyle: "short",
                  }
                )}
              />
              <DeleteButton onClick={() => removeArbeidsgiverperiode(id)} />
            </div>
          </Card>
        )}
      </For>
    </>
  );
};
