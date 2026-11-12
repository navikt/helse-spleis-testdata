import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";

type PeriodeId = string;

export const Utenlandsperioder = React.memo(() => {
  const { unregister, watch } = useFormContext();
  const sykdomFom = watch("sykdomFom");
  const sykdomTom = watch("sykdomTom");

  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  const addUtenlandsperiode = () => {
    setPerioder((old) => [...old, nanoid()]);
  };

  const removeUtenlandsperiode = (index: number) => {
    unregister(`søknad.utenlandsperioder`);
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton onClick={addUtenlandsperiode} data-testid="utenlandsButton">
        Legg inn utenlandsopphold
      </AddButton>
      {perioder.map((id, i) => (
        <Card key={id}>
          <HStack gap="space-16" align="end" wrap={false}>
            <FormDatePicker
              data-testid={`utenlandsFom${i}`}
              label="Utenlandsopphold f.o.m."
              name={`søknad.utenlandsperioder.${i}.fom`}
              defaultValue={sykdomFom}
              rules={{ required: "Start av utenlandsoppholdet må angis" }}
            />
            <FormDatePicker
              data-testid={`utenlandsTom${i}`}
              label="Utenlandsopphold t.o.m."
              name={`søknad.utenlandsperioder.${i}.tom`}
              defaultValue={sykdomTom}
              rules={{ required: "Slutt av utenlandsoppholdet må angis" }}
            />
            <DeleteButton
              aria-label="Fjern utenlandsopphold"
              onClick={() => removeUtenlandsperiode(i)}
            />
          </HStack>
        </Card>
      ))}
    </>
  );
});
