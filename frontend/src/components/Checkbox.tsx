import React from "react";
import { Checkbox as AkselCheckbox, ErrorMessage } from "@navikt/ds-react";
import type { FieldErrors } from "react-hook-form";

interface CheckboxProps extends Omit<
  React.InputHTMLAttributes<HTMLInputElement>,
  "size" | "type"
> {
  label: string;
  errors?: FieldErrors;
}

export const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ id, name, label, errors, className, ...rest }, ref) => {
    const feilmelding =
      name && errors?.[name]
        ? (errors[name]!.message as string | undefined)
        : undefined;

    return (
      <div className={className}>
        <AkselCheckbox
          id={id}
          name={name}
          size="small"
          error={feilmelding !== undefined}
          ref={ref}
          {...rest}
        >
          {label}
        </AkselCheckbox>
        {feilmelding && <ErrorMessage size="small">{feilmelding}</ErrorMessage>}
      </div>
    );
  },
);
