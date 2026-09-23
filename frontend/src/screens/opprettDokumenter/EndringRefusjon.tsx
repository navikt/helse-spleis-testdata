import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormInput } from "../../components/FormInput";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";
import { validateInntekt } from "../formValidation";

type OpphørId = string;

export const EndringRefusjon = React.memo(() => {
  const { register, unregister, formState } = useFormContext();

  const [opphør, setOpphør] = useState<OpphørId[]>([]);

  const addEndring = () => {
    setOpphør((old) => [...old, nanoid()]);
  };

  const removeEndring = (index: number) => {
    unregister(`inntektsmelding.endringIRefusjon`);
    setOpphør((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addEndring} data-testid="endringButton">
        Legg inn endring i refusjon
      </AddButton>
      {opphør.map((id, i) => (
        <Card key={id}>
          <VStack gap="space-16">
            <HStack gap="space-16" align="end" wrap={false}>
              <FormDatePicker
                data-testid={`endringsdato${i}`}
                label="Dato for endring"
                name={`inntektsmelding.endringIRefusjon.${i}.endringsdato`}
                defaultValue="2021-07-01"
                rules={{ required: "Dato for endring må angis" }}
              />
              <DeleteButton
                aria-label="Fjern endring i refusjon"
                onClick={() => removeEndring(i)}
              />
            </HStack>
            <FormInput
              data-testid={`endringsbeløp${i}`}
              label="Beløp for endring"
              errors={formState.errors}
              {...register(
                `inntektsmelding.endringIRefusjon.${i}.endringsbeløp`,
                {
                  required: "Beløp for endring må angis",
                  validate: validateInntekt,
                },
              )}
            />
          </VStack>
        </Card>
      ))}
    </>
  );
});
