import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";

type PeriodeId = string;

export const Arbeidsgiverperioder = React.memo(() => {
  const { unregister } = useFormContext();
  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  const addArbeidsgiverperiode = () => {
    setPerioder((old) => [...old, nanoid()]);
  };

  const removeArbeidsgiverperiode = (index: number) => {
    unregister(`inntektsmelding.arbeidsgiverperiode`);
    setPerioder((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  return (
    <>
      <AddButton
        onClick={addArbeidsgiverperiode}
        data-testid="arbeidsgiverperioderButton"
      >
        Legg inn arbeidsgiverperioder
      </AddButton>
      {perioder.map((id, i) => (
        <Card key={id}>
          <HStack gap="space-16" align="end" wrap={false}>
            <FormDatePicker
              data-testid={`arbeidsgiverFom${i}`}
              label="Arbeidsgiverperiode f.o.m."
              name={`inntektsmelding.arbeidsgiverperiode.${i}.fom`}
              defaultValue="2021-07-01"
              rules={{ required: "Start av arbeidsgiverperioden må angis" }}
            />
            <FormDatePicker
              data-testid={`arbeidsgiverTom${i}`}
              label="Arbeidsgiverperiode t.o.m."
              name={`inntektsmelding.arbeidsgiverperiode.${i}.tom`}
              defaultValue="2021-07-16"
              rules={{ required: "Slutt av arbeidsgiverperioden må angis" }}
            />
            <DeleteButton
              aria-label="Fjern arbeidsgiverperiode"
              onClick={() => removeArbeidsgiverperiode(i)}
            />
          </HStack>
        </Card>
      ))}
    </>
  );
});
