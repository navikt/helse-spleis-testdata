import React from "react";
import { InputLabel } from "./InputLabel";
import { Input } from "./Input";
import { ErrorMessage } from "./ErrorMessage";
import type { FieldErrors } from "react-hook-form";
import classNames from "classnames";
import { nanoid } from "nanoid";
import {Select} from "./Select";

interface Option {
    value: string
    label: string
}
interface FormSelectProps extends React.InputHTMLAttributes<HTMLSelectElement> {
  label: string;
  options: (string|Option)[]
}

export const FormSelect = React.forwardRef<HTMLSelectElement, FormSelectProps>(
  ({ name, label, options, ...rest }, ref) => {
    const id = nanoid();
    return (
      <InputLabel>
        {label}
        <Select
          id={id}
          name={name}
          options={options}
          ref={ref}
          {...rest}
        />
      </InputLabel>
    );
  }
);
