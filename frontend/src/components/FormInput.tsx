import React from "react";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";
import type { FieldErrors } from "react-hook-form";
import classNames from "classnames";
import { nanoid } from "nanoid";

interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errors: FieldErrors;
}

export const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ name, label, type, errors, ...rest }, ref) => {
    const id = nanoid();
    return (
      <InputLabel>
        {label}
        <Input
          id={id}
          name={name}
          type={type ?? "text"}
          className={classNames(errors[name] && "error")}
          aria-invalid={errors[name] ? "true" : "false"}
          {...rest}
          ref={ref}
        />
        {errors[name] && (
          <ErrorMessage label-for={id}>{errors[name].message}</ErrorMessage>
        )}
      </InputLabel>
    );
  }
);
