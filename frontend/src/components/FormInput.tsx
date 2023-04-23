import React from "react";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";
import type { FieldErrors } from "react-hook-form";
import classNames from "classnames";
import { nanoid } from "nanoid";
import {Simulate} from "react-dom/test-utils";
import input = Simulate.input;

interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errors?: FieldErrors;
}

export const FormInput = React.forwardRef<HTMLInputElement, FormInputProps>(
  ({ name, label, type, errors, ...rest }, ref) => {
    const id = nanoid();
    var inputErrors = errors
    name.split('.').forEach(it => {
        if (inputErrors) inputErrors = inputErrors[it]
    })
    return (
      <InputLabel>
        {label}
        <Input
          id={id}
          name={name}
          type={type ?? "text"}
          className={classNames(inputErrors && "error")}
          aria-invalid={inputErrors ? "true" : "false"}
          {...rest}
          ref={ref}
        />
        {inputErrors && (
          <ErrorMessage label-for={id}>{inputErrors.message}</ErrorMessage>
        )}
      </InputLabel>
    );
  }
);
