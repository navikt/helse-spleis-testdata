import styles from "./OpprettDokumenter.module.css";
import { nanoid } from "nanoid";
import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import type { Component } from "solid-js";
import { createSignal, For } from "solid-js";
import { useFormContext } from "../../state/useFormContext";

export const EndringRefusjon: Component = () => {
  const { register, deregister, errors } = useFormContext();
  const [opphør, setOpphør] = createSignal<string[]>([]);

  const addEndring = () => {
    setOpphør((old) => [...old, nanoid()]);
  };

  const removeEndring = (id: string) => {
    const index = opphør().findIndex((it) => it === id);
    deregister(`endring${id}`);
    setOpphør((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addEndring}>Legg inn endring i refusjon</AddButton>
      <For each={opphør()}>
        {(id: string) => (
          <Card>
            <div class={styles.PeriodContainer}>
              <FormInput
                register={register}
                errors={errors}
                label="Dato for endring"
                name={`endring${id}`}
                id={`endring${id}`}
                type="date"
                required
                defaultValue={new Date("2020-01-01").toLocaleDateString(
                  "nb-NO",
                  {
                    dateStyle: "short",
                  }
                )}
              />
              <DeleteButton onClick={() => removeEndring(id)} />
            </div>
          </Card>
        )}
      </For>
    </>
  );
};
