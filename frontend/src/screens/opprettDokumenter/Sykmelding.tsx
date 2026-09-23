import React from "react";
import { useFormContext } from "react-hook-form";
import { VStack } from "@navikt/ds-react";

import { FormInput } from "../../components/FormInput";
import { validateSykdomsgrad } from "../formValidation";

export const Sykmelding = React.memo(() => {
  const { register, formState } = useFormContext();

  return (
    <VStack gap="space-16">
      <FormInput
        label="Sykdomsgrad i sykmeldingen"
        errors={formState.errors}
        defaultValue={100}
        {...register("sykmeldingsgrad", {
          required: "Sykmeldingsgrad må angis",
          validate: validateSykdomsgrad,
        })}
      />
    </VStack>
  );
});
