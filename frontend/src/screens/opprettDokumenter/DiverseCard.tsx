import React from "react";
import { useFieldArray, useFormContext } from "react-hook-form";
import { Heading, HStack, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { Sykmelding } from "./Sykmelding";
import { FormSelect } from "../../components/FormSelect";
import { FormInput } from "../../components/FormInput";
import { AddButton } from "../../components/AddButton";
import { DeleteButton } from "../../components/DeleteButton";

export const DiverseCard = React.memo(() => {
  const { register, watch } = useFormContext();
  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const medlemskapVerdi = watch("medlemskapVerdi");

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Diverse
        </Heading>
        <FormSelect
          label="Medlemskapsvurdering"
          options={["JA", "NEI", "UAVKLART", "UAVKLART_MED_BRUKERSPORSMAAL"]}
          data-testid="medlemskapVerdi"
          {...register("medlemskapVerdi")}
        />
        {medlemskapVerdi && medlemskapVerdi !== "JA" && <MedlemskapAarsaker />}
        {skalSendeSykmelding && <Sykmelding />}
      </VStack>
    </Card>
  );
});

const MedlemskapAarsaker = () => {
  const {
    register,
    control,
    formState: { errors },
  } = useFormContext();
  const { fields, append, remove } = useFieldArray({
    control,
    name: "medlemskapAarsaker",
  });

  return (
    <VStack gap="space-8">
      {fields.map((field, i) => (
        <HStack key={field.id} gap="space-8" align="end" wrap={false}>
          <FormInput
            data-testid={`medlemskapAarsakRegelId${i}`}
            label="Regel-ID"
            errors={errors}
            {...register(`medlemskapAarsaker.${i}.regelId`, {
              required: "Regel-ID må angis",
            })}
          />
          <FormInput
            data-testid={`medlemskapAarsakForklaring${i}`}
            label="Forklaring på uavklart"
            errors={errors}
            {...register(`medlemskapAarsaker.${i}.forklaringPaaUavklart`, {
              required: "Forklaring må angis",
            })}
          />
          <DeleteButton aria-label="Fjern årsak" onClick={() => remove(i)} />
        </HStack>
      ))}
      <HStack justify="start">
        <AddButton
          data-testid="leggTilMedlemskapAarsak"
          onClick={() => append({ regelId: "", forklaringPaaUavklart: "" })}
        >
          Legg til årsak
        </AddButton>
      </HStack>
    </VStack>
  );
};
