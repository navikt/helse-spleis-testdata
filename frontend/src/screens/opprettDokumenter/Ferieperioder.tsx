import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";

type PeriodeId = string;

export const Ferieperioder = React.memo(() => {
  const { unregister } = useFormContext();

  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  const addFerieperiode = () => {
    setPerioder((old) => [...old, nanoid()]);
  };

  const removeFerieperiode = (index: number) => {
    unregister(`søknad.ferieperioder`);
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addFerieperiode} data-testid="ferieButton">
        Legg inn ferieperioder
      </AddButton>
      {perioder.map((id, i) => (
        <Card key={id}>
          <HStack gap="space-16" align="end" wrap={false}>
            <FormDatePicker
              data-testid={`ferieFom${i}`}
              label="Ferieperiode f.o.m."
              name={`søknad.ferieperioder.${i}.fom`}
              defaultValue="2021-07-01"
              rules={{ required: "Start av ferieperioden må angis" }}
            />
            <FormDatePicker
              data-testid={`ferieTom${i}`}
              label="Ferieperiode t.o.m."
              name={`søknad.ferieperioder.${i}.tom`}
              defaultValue="2021-07-10"
              rules={{ required: "Slutt av ferieperioden må angis" }}
            />
            <DeleteButton
              aria-label="Fjern ferieperiode"
              onClick={() => removeFerieperiode(i)}
            />
          </HStack>
        </Card>
      ))}
    </>
  );
});
