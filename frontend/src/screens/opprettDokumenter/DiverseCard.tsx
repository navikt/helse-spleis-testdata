import React from "react";
import { useFormContext } from "react-hook-form";
import { Heading, VStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { Sykmelding } from "./Sykmelding";
import { FormSelect } from "../../components/FormSelect";

export const DiverseCard = React.memo(() => {
  const { register, setValue, watch } = useFormContext();
  const skalSendeSykmelding = watch("skalSendeSykmelding");
  const lovmeMockFinnes = false; // akkurat nå finnes det ikke noen LovMe-mock som kan plukke opp Medlemskapsvurdering

  return (
    <Card>
      <VStack gap="space-16">
        <Heading level="2" size="small">
          Diverse
        </Heading>
        {lovmeMockFinnes && (
          <FormSelect
            label="Medlemskapsvurdering"
            options={["JA", "NEI", "UAVKLART", "UAVKLART_MED_BRUKERSPORSMAAL"]}
            {...register("medlemskapVerdi")}
            onChange={(val) => {
              const verdi =
                val.target.options[val.target.options.selectedIndex].value;
              setValue("medlemskapVerdi", verdi);
            }}
          />
        )}
        {skalSendeSykmelding && <Sykmelding />}
      </VStack>
    </Card>
  );
});
