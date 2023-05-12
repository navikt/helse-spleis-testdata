import React from "react";
import {useFormContext} from "react-hook-form";
import {FormInput} from "../../components/FormInput";
import addDays from "date-fns/addDays";
import format from "date-fns/format";
import {endOfMonth, subMonths} from "date-fns";

export const SykdomTom = () => {
  const { watch, formState, register, setValue } = useFormContext();

  const sykdomFom = watch("sykdomFom");

  const sykdomTomRegister = register("sykdomTom", {
    required: "Slutt av sykdomsforløp må angis",
    validate: (value: string): boolean | string =>
        (new Date(value) >= new Date(sykdomFom) || 'Tom kan ikke være tidligere enn fom'),
  });

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setValue(
      "søknad.sendtNav",
      format(addDays(new Date(event.target.value), 1), "yyyy-MM-dd")
    );
    return sykdomTomRegister.onChange(event);
  };

  const defaultTom = format(endOfMonth(subMonths(new Date(), 3)), "yyyy-MM-dd")

  return (
    <FormInput
      data-testid="sykdomTom"
      label="Sykdom t.o.m."
      errors={formState.errors}
      type="date"
      defaultValue={defaultTom}
      {...sykdomTomRegister}
      onChange={onChange}
    />
  );
};
