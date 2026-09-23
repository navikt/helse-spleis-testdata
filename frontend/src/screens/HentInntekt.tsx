import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { ErrorMessage, Heading, VStack } from "@navikt/ds-react";

import { get } from "../io/api";
import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { CopyField } from "../components/CopyField";
import { FetchButton } from "../components/FetchButton";
import { validateFødselsnummer } from "./formValidation";

export const HentInntekt = React.memo(() => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [inntekt, setInntekt] = useState<number>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setIsFetching(true);
    get("/person/inntekt", { ident: data.fnr })
      .then(async (response) => {
        const { beregnetMånedsinntekt } = await response.json();
        setInntekt(beregnetMånedsinntekt);
        setStatus(response.status);
      })
      .catch((_) => setStatus(404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <VStack gap="space-32" padding="space-32" align="start">
        <Card>
          <VStack gap="space-16" minWidth="300px" align="start">
            <Heading level="2" size="small">
              Hent inntekt
            </Heading>
            <FormInput
              id="fnr"
              label="Fødselsnummer"
              errors={errors}
              {...register("fnr", {
                required: "Fødselsnummer må fylles ut",
                validate: validateFødselsnummer,
              })}
            />
            <FetchButton status={status} isFetching={isFetching}>
              Hent inntekt
            </FetchButton>
            {typeof status === "number" && status >= 400 && (
              <ErrorMessage>Kunne ikke hente inntekt</ErrorMessage>
            )}
          </VStack>
        </Card>
        <Card>
          <CopyField value={String(inntekt ?? "")} label="Inntekt" />
        </Card>
      </VStack>
    </form>
  );
});
