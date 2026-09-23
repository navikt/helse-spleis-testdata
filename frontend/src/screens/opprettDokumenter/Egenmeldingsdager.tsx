import React, { useState } from "react";
import { nanoid } from "nanoid";
import { useFormContext } from "react-hook-form";
import { format, subDays } from "date-fns";
import { HStack } from "@navikt/ds-react";

import { Card } from "../../components/Card";
import { FormDatePicker } from "../../components/FormDatePicker";
import { DeleteButton } from "../../components/DeleteButton";
import { AddButton } from "../../components/AddButton";

type DagId = string;

export const Egenmeldingsdager = React.memo(() => {
  const { watch, unregister } = useFormContext();

  const [dager, setDager] = useState<DagId[]>([]);

  const addEgenmeldingsdager = () => {
    setDager((old) => [...old, nanoid()]);
  };

  const removeEgenmeldingsdager = (index: number) => {
    unregister(`søknad.egenmeldingsdager`);
    setDager((old) => [...old.slice(0, index), ...old.slice(index + 1)]);
  };

  const sykdomFom = watch("sykdomFom");

  return (
    <>
      <AddButton
        onClick={addEgenmeldingsdager}
        data-testid="egenmeldingsButton"
      >
        Legg inn egenmeldingsdager
      </AddButton>
      {dager.map((id, i) => (
        <Card key={id}>
          <HStack gap="space-16" align="end" wrap={false}>
            <FormDatePicker
              data-testid={`egenmeldingsdag${i}`}
              label="Egenmeldingsdag"
              name={`søknad.egenmeldingsdager.${i}`}
              defaultValue={format(
                subDays(new Date(sykdomFom), i + 1),
                "yyyy-MM-dd",
              )}
              rules={{ required: "Dato for egenmelding må angis" }}
            />
            <DeleteButton
              aria-label="Fjern egenmeldingsdag"
              onClick={() => removeEgenmeldingsdager(i)}
            />
          </HStack>
        </Card>
      ))}
    </>
  );
});
