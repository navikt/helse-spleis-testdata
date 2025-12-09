import React from "react";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";
import type { FieldErrors } from "react-hook-form";
import classNames from "classnames";
import { nanoid } from "nanoid";

interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errors?: FieldErrors;
}

export const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ name, label, type, errors, ...rest }, ref) => {
    const id = nanoid();
    const errorMessage = getErrorMessage(name, errors);

    return (
      <InputLabel>
        {label}
        <Input
          id={id}
          name={name}
          type={type ?? "text"}
          className={classNames(errorMessage != undefined && "error")}
          aria-invalid={errorMessage != undefined ? "true" : "false"}
          {...rest}
          ref={ref}
        />
        {errorMessage && (
          <ErrorMessage label-for={id}>{errorMessage}</ErrorMessage>
        )}
      </InputLabel>
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
