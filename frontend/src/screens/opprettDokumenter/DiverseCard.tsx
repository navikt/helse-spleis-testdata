import React from "react";
import { useFieldArray, useFormContext } from "react-hook-form";
import { Heading, HStack, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { Sykmelding } from "./Sykmelding";
import { FormSelect } from "../../components/FormSelect";
import { FormInput } from "../../components/FormInput";
import { AddButton } from "../../components/AddButton";
import { DeleteButton } from "../../components/DeleteButton";

const medlemskapssvar = [
  "JA",
  "NEI",
  "UAVKLART",
  "UAVKLART_MED_BRUKERSPORSMAAL",
];

export const IKKE_SEND = "IKKE_SEND";

export const DiverseCard = React.memo(() => {
  const { register, watch } = useFormContext();
  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const skalSendeSøknad = watch("skalSendeSøknad");
  const medlemskapVerdi = watch("medlemskapVerdi");
  const forhandsvurderingSvar = watch("forhandsvurderingSvar");

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Diverse
        </Heading>
        <FormSelect
          label="Medlemskapsvurdering (mock ved synkront oppslag)"
          options={[
            ...medlemskapssvar,
            { value: "IKKE_MOCK", label: "Ikke mock – slå opp i LovMe" },
          ]}
          data-testid="medlemskapVerdi"
          {...register("medlemskapVerdi")}
        />
        {medlemskapVerdi &&
          medlemskapVerdi !== "JA" &&
          medlemskapVerdi !== "IKKE_MOCK" && (
            <MedlemskapAarsaker prefix="medlemskap" />
          )}
        {skalSendeSøknad && (
          <>
            <FormSelect
              label="Forhåndsvurdering fra LovMe"
              options={[
                ...medlemskapssvar,
                { value: IKKE_SEND, label: "Ikke send" },
              ]}
              data-testid="forhandsvurderingSvar"
              {...register("forhandsvurderingSvar")}
            />
            {forhandsvurderingSvar &&
              forhandsvurderingSvar !== "JA" &&
              forhandsvurderingSvar !== IKKE_SEND && (
                <MedlemskapAarsaker prefix="forhandsvurdering" />
              )}
          </>
        )}
        {skalSendeSykmelding && <Sykmelding />}
      </VStack>
    </Card>
  );
});

const MedlemskapAarsaker = ({
  prefix,
}: {
  prefix: "medlemskap" | "forhandsvurdering";
}) => {
  const name = `${prefix}Aarsaker`;
  const {
    register,
    control,
    formState: { errors },
  } = useFormContext();
  const { fields, append, remove } = useFieldArray({
    control,
    name,
  });

  return (
    <VStack gap="space-8">
      {fields.map((field, i) => (
        <HStack key={field.id} gap="space-8" align="end" wrap={false}>
          <FormInput
            data-testid={`${prefix}AarsakRegelId${i}`}
            label="Regel-ID"
            errors={errors}
            {...register(`${name}.${i}.regelId`, {
              required: "Regel-ID må angis",
            })}
          />
          <FormInput
            data-testid={`${prefix}AarsakForklaring${i}`}
            label="Forklaring på uavklart"
            errors={errors}
            {...register(`${name}.${i}.forklaringPaaUavklart`, {
              required: "Forklaring må angis",
            })}
          />
          <DeleteButton aria-label="Fjern årsak" onClick={() => remove(i)} />
        </HStack>
      ))}
      <HStack justify="start">
        <AddButton
          data-testid={`${prefix}LeggTilAarsak`}
          onClick={() => append({ regelId: "", forklaringPaaUavklart: "" })}
        >
          Legg til årsak
        </AddButton>
      </HStack>
    </VStack>
  );
};
