import React from "react";
import { useController, useFormContext } from "react-hook-form";
import type { RegisterOptions } from "react-hook-form";

import { DatoFelt } from "./DatoFelt";

interface FormDatePickerProps {
  name: string;
  label: string;
  /** ISO-dato (yyyy-MM-dd). */
  defaultValue?: string;
  rules?: RegisterOptions;
  /** Kalles med ISO-dato, eller tom streng når feltet ikke inneholder en gyldig dato. */
  onDateChange?: (isoDato: string) => void;
  hideLabel?: boolean;
  disabled?: boolean;
  "data-testid"?: string;
}

export const FormDatePicker = ({
  name,
  label,
  defaultValue,
  rules,
  onDateChange,
  hideLabel,
  disabled,
  "data-testid": dataTestId,
}: FormDatePickerProps) => {
  const { control } = useFormContext();
  const { field, fieldState } = useController({
    name,
    control,
    rules,
    defaultValue: defaultValue ?? "",
  });

  return (
    <DatoFelt
      label={label}
      verdi={typeof field.value === "string" ? field.value : ""}
      onEndret={(isoDato) => {
        field.onChange(isoDato);
        onDateChange?.(isoDato);
      }}
      name={field.name}
      error={fieldState.error?.message}
      hideLabel={hideLabel}
      disabled={disabled}
      onBlur={field.onBlur}
      data-testid={dataTestId}
      ref={field.ref}
    />
  );
};
