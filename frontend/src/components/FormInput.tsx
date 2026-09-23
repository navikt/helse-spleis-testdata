import React from "react";
import { TextField } from "@navikt/ds-react";
import type { FieldErrors } from "react-hook-form";

type TextFieldType = React.ComponentProps<typeof TextField>["type"];

interface FormInputProps extends Omit<
  React.InputHTMLAttributes<HTMLInputElement>,
  "size" | "type" | "value" | "defaultValue"
> {
  label: string;
  type?: TextFieldType;
  value?: string | number;
  defaultValue?: string | number;
  errors?: FieldErrors;
}

export const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ name, label, type, errors, ...rest }, ref) => {
    const errorMessage = getErrorMessage(name, errors);

    return (
      <TextField
        label={label}
        size="small"
        name={name}
        type={type ?? "text"}
        error={errorMessage}
        {...rest}
        ref={ref}
      />
    );
  },
);

function getErrorMessage(
  name: string | undefined,
  errors: FieldErrors | undefined,
): string | undefined {
  if (!name || !errors) return undefined;

  const path = name.split(".");
  let current: unknown = errors;

  for (const key of path) {
    if (current && typeof current === "object" && key in current) {
      current = (current as Record<string, unknown>)[key];
    } else {
      return undefined;
    }
  }

  if (current && typeof current === "object" && "message" in current) {
    return (current as { message?: string }).message;
  }

  return undefined;
}
