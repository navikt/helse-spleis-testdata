import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { ErrorMessage, Heading, VStack } from "@navikt/ds-react";

import { Card } from "../components/Card";
import { FormInput } from "../components/FormInput";
import { FetchButton } from "../components/FetchButton";
import { get } from "../io/api";
import { validateGruppeId } from "./formValidation";

export const HentTestgruppe: React.FC = () => {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();

  const [status, setStatus] = useState<number>();
  const [isFetching, setIsFetching] = useState(false);

  const onSubmit = (data: Record<string, any>) => {
    setStatus(undefined);
    setIsFetching(true);
    get(`/gruppe/${data.gruppeId}`)
      .then((response) => setStatus(response.status))
      .catch((error) => setStatus(error.status ?? 404))
      .finally(() => setIsFetching(false));
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <VStack gap="space-32" padding="space-32" align="start">
        <Card>
          <VStack gap="space-16" minWidth="300px" align="start">
            <Heading level="2" size="small">
              Hent testgruppe
            </Heading>
            <FormInput
              id="gruppeId"
              label="Gruppe-ID"
              errors={errors}
              {...register("gruppeId", {
                required: "Gruppe-ID må fylles ut",
                validate: validateGruppeId,
              })}
            />
            <FetchButton status={status} isFetching={isFetching}>
              Hent gruppe
            </FetchButton>
            {typeof status === "number" && status > 400 && (
              <ErrorMessage>
                Det skjedde en feil. Prøv igjen senere.
              </ErrorMessage>
            )}
          </VStack>
        </Card>
      </VStack>
    </form>
  );
};
