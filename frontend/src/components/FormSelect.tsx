import React from "react";
import { Select } from "@navikt/ds-react";

interface Option {
  value: string;
  label: string;
}

interface FormSelectProps extends Omit<
  React.SelectHTMLAttributes<HTMLSelectElement>,
  "size"
> {
  label: string;
  options: (string | Option)[];
}

export const FormSelect = React.forwardRef<HTMLSelectElement, FormSelectProps>(
  ({ name, label, options, ...rest }, ref) => (
    <Select label={label} size="small" name={name} ref={ref} {...rest}>
      {options.map((option, index) => {
        const label = typeof option === "object" ? option.label : option;
        const value = typeof option === "object" ? option.value : option;
        return (
          <option value={value} key={index}>
            {label}
          </option>
        );
      })}
    </Select>
  ),
);
