import React from "react";
import { useFormContext } from "react-hook-form";
import { FormDatePicker } from "../../components/FormDatePicker";
import { format, startOfMonth, subMonths } from "date-fns";

export const SykdomFom = () => {
  const { getValues, setValue } = useFormContext();

  const defaultFom = format(
    startOfMonth(subMonths(new Date(), 3)),
    "yyyy-MM-dd",
  );

  return (
    <FormDatePicker
      data-testid="sykdomFom"
      label="Sykdom f.o.m."
      name="sykdomFom"
      defaultValue={defaultFom}
      rules={{
        required: "Start av sykdomsforløp må angis",
        validate: (value: string): boolean | string =>
          new Date(value) <= new Date(getValues("sykdomTom")) ||
          "Fom kan ikke være senere enn tom",
      }}
      onDateChange={(isoDato) =>
        setValue("inntektsmelding.førsteFraværsdag", isoDato)
      }
    />
  );
};
