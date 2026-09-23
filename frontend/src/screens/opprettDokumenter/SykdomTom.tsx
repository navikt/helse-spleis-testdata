import React from "react";
import { useFormContext } from "react-hook-form";
import { FormDatePicker } from "../../components/FormDatePicker";
import { addDays, endOfMonth, format, subMonths } from "date-fns";

export const SykdomTom = () => {
  const { getValues, setValue } = useFormContext();

  const defaultTom = format(endOfMonth(subMonths(new Date(), 3)), "yyyy-MM-dd");

  return (
    <FormDatePicker
      data-testid="sykdomTom"
      label="Sykdom t.o.m."
      name="sykdomTom"
      defaultValue={defaultTom}
      rules={{
        required: "Slutt av sykdomsforløp må angis",
        validate: (value: string): boolean | string =>
          new Date(value) >= new Date(getValues("sykdomFom")) ||
          "Tom kan ikke være tidligere enn fom",
      }}
      onDateChange={(isoDato) =>
        setValue(
          "søknad.sendtNav",
          isoDato === ""
            ? ""
            : format(addDays(new Date(isoDato), 1), "yyyy-MM-dd"),
        )
      }
    />
  );
};
