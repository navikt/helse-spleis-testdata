import React from "react";
import { useFormContext } from "react-hook-form";
import { FormInput } from "../../components/FormInput";

export const SykdomFom = () => {
  const { formState, register, setValue } = useFormContext();

  const sykdomFomRegister = register("sykdomFom", {
    required: "Start av sykdomsforløp må angis",
  });

  const onChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setValue("førsteFraværsdag", event.target.value);
    return sykdomFomRegister.onChange(event);
  };

  return (
    <FormInput
      data-testid="sykdomFom"
      label="Sykdom f.o.m."
      errors={formState.errors}
      type="date"
      defaultValue="2021-07-01"
      {...sykdomFomRegister}
      onChange={onChange}
    />
  );
};
