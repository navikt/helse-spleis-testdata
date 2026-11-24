import React, { useEffect, useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { HStack } from "@navikt/ds-react";

import { validatePeriodeInnenforSøknadsperioden } from "../formValidation";
import { Card } from "../../components/Card";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";

type PeriodeId = string;

export const Utenlandsperioder = React.memo(() => {
  const { unregister, watch, getValues, trigger } = useFormContext();
  const sykdomFom = watch("sykdomFom");
  const sykdomTom = watch("sykdomTom");

  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  useEffect(() => {
    if (getValues("søknad.utenlandsperioder")?.length) {
      void trigger("søknad.utenlandsperioder");
    }
  }, [getValues, sykdomFom, sykdomTom, trigger]);

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
              rules={{
                required: "Start av utenlandsoppholdet må angis",
                validate: (dato) =>
                  validatePeriodeInnenforSøknadsperioden({
                    dato,
                    fom: dato,
                    tom: getValues(`søknad.utenlandsperioder.${i}.tom`),
                    sykdomFom,
                    sykdomTom,
                    feilmeldingUtenfor:
                      "Utenlandsoppholdet må være innenfor søknadsperioden",
                  }),
              }}
            />
            <FormDatePicker
              data-testid={`utenlandsTom${i}`}
              label="Utenlandsopphold t.o.m."
              name={`søknad.utenlandsperioder.${i}.tom`}
              defaultValue={sykdomTom}
              rules={{
                required: "Slutt av utenlandsoppholdet må angis",
                validate: (dato) =>
                  validatePeriodeInnenforSøknadsperioden({
                    dato,
                    fom: getValues(`søknad.utenlandsperioder.${i}.fom`),
                    tom: dato,
                    sykdomFom,
                    sykdomTom,
                    feilmeldingUtenfor:
                      "Utenlandsoppholdet må være innenfor søknadsperioden",
                  }),
              }}
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
