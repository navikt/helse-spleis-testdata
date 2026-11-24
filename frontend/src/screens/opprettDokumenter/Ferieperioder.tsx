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

export const Ferieperioder = React.memo(() => {
  const { unregister, watch, getValues, trigger } = useFormContext();
  const sykdomFom = watch("sykdomFom");
  const sykdomTom = watch("sykdomTom");

  const [perioder, setPerioder] = useState<PeriodeId[]>([]);

  useEffect(() => {
    if (getValues("søknad.ferieperioder")?.length) {
      void trigger("søknad.ferieperioder");
    }
  }, [getValues, sykdomFom, sykdomTom, trigger]);

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
              defaultValue={sykdomFom}
              rules={{
                required: "Start av ferieperioden må angis",
                validate: (dato) =>
                  validatePeriodeInnenforSøknadsperioden({
                    dato,
                    fom: dato,
                    tom: getValues(`søknad.ferieperioder.${i}.tom`),
                    sykdomFom,
                    sykdomTom,
                    feilmeldingUtenfor:
                      "Ferieperioden må være innenfor søknadsperioden",
                  }),
              }}
            />
            <FormDatePicker
              data-testid={`ferieTom${i}`}
              label="Ferieperiode t.o.m."
              name={`søknad.ferieperioder.${i}.tom`}
              defaultValue={sykdomTom}
              rules={{
                required: "Slutt av ferieperioden må angis",
                validate: (dato) =>
                  validatePeriodeInnenforSøknadsperioden({
                    dato,
                    fom: getValues(`søknad.ferieperioder.${i}.fom`),
                    tom: dato,
                    sykdomFom,
                    sykdomTom,
                    feilmeldingUtenfor:
                      "Ferieperioden må være innenfor søknadsperioden",
                  }),
              }}
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
