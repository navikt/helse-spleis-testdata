import styles from "./HentAktørId.module.css";
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

const numerical = (value: string, message: string): false | string =>
  isNaN(Number.parseInt(value)) && message;

const validFnr = (value: string): false | string =>
  numerical(value, "Fødselsnummeret må være numerisk") ||
  (value.length !== 11 && "Fødselsnummeret må bestå av 11 siffere");

export const HentAktørId: Component = () => {
  const { register, errors, values } = useForm();
  const [status, setStatus] = createSignal<number>();
  const [isFetching, setIsFetching] = createSignal(false);
  const [aktørId, setAktørId] = createSignal<string>();

  const onSubmit = async (event: Event) => {
    event.preventDefault();
    setIsFetching(true);

    const response = await get("/person/aktorid", { ident: values().fnr })
      .then(async (response) => {
        const aktørId = await response.text();
        setAktørId(aktørId);
        setStatus(response.status);
      })
      .catch((error) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <Fade>
      <form onSubmit={onSubmit}>
        <div class={styles.HentAktørId}>
          <Card>
            <h2 class={styles.Title}>Hent aktør-ID</h2>
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
                Hent aktør-ID
              </FetchButton>
              <Show when={status() >= 400}>
                <ErrorMessage>Kunne ikke hente aktør-ID</ErrorMessage>
              </Show>
            </div>
          </Card>
          <Card>
            <CopyField value={String(aktørId() ?? "")} label="Aktør-ID" />
          </Card>
        </div>
      </form>
    </Fade>
  );
};
