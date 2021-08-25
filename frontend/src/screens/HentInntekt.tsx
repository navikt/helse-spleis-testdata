import styles from "./HentInntekt.module.css";
import type { Component } from "solid-js";
import { createSignal, Show } from "solid-js";
import { Fade } from "../components/Fade";
import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { useForm } from "../state/useForm";
import { FetchButton } from "../components/FetchButton";
import { CopyField } from "../components/CopyField";
import { get } from "../io/api";
import { ErrorMessage } from "../components/ErrorMessage";

type HentInntektResponse = {
  beregnetMånedsinntekt: number;
};

const numerical = (value: string, message: string): false | string =>
  isNaN(Number.parseInt(value)) && message;

const validFnr = (value: string): false | string =>
  numerical(value, "Fødselsnummeret må være numerisk") ||
  (value.length !== 11 && "Fødselsnummeret må bestå av 11 siffere");

export const HentInntekt: Component = () => {
  const { register, errors, values } = useForm();
  const [status, setStatus] = createSignal<number>();
  const [isFetching, setIsFetching] = createSignal(false);
  const [inntekt, setInntekt] = createSignal<number>();

  const onSubmit = async (event: Event) => {
    event.preventDefault();
    setIsFetching(true);

    const response = await get("/person/inntekt", { ident: values().fnr })
      .then(async (response) => {
        const { beregnetMånedsinntekt } = await response.json();
        setInntekt(beregnetMånedsinntekt);
        setStatus(response.status);
      })
      .catch((error) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <Fade>
      <form onSubmit={onSubmit}>
        <div class={styles.HentInntekt}>
          <Card>
            <h2 class={styles.Title}>Hent inntekt</h2>
            <div class={styles.CardContainer}>
              <FormInput
                register={register}
                validation={validFnr}
                errors={errors}
                label="Fødselsnummer"
                name="fnr"
                id="fnr"
                required
              />
              <FetchButton status={status()} isFetching={isFetching()}>
                Hent inntekt
              </FetchButton>
              <Show when={status() >= 400}>
                <ErrorMessage>Kunne ikke hente inntekt</ErrorMessage>
              </Show>
            </div>
          </Card>
          <Card>
            <CopyField value={String(inntekt() ?? "")} label="Inntekt" />
          </Card>
        </div>
      </form>
    </Fade>
  );
};
