import React from "react";
import { useFormContext } from "react-hook-form";
import { FormInput } from "../../components/FormInput";
import {startOfMonth, subMonths} from "date-fns";
import format from "date-fns/format";

export const SykdomFom = () => {
  const { getValues, formState, register, setValue } = useFormContext();

  const sykdomFomRegister = register("sykdomFom", {
    required: "Start av sykdomsforløp må angis",
    validate: (value: string): boolean | string =>
        (new Date(value) <= new Date(getValues("sykdomTom")) || 'Fom kan ikke være senere enn tom'),
  });

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setValue("inntektsmelding.førsteFraværsdag", event.target.value);
    return sykdomFomRegister.onChange(event);
  };

  const defaultFom = format(startOfMonth(subMonths(new Date(), 3)), "yyyy-MM-dd")

  return (
    <FormInput
      data-testid="sykdomFom"
      label="Sykdom f.o.m."
      errors={formState.errors}
      type="date"
      defaultValue={defaultFom}
      {...sykdomFomRegister}
      onChange={onChange}
    />
  );
};
