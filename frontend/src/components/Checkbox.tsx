import styles from "./Checkbox.module.css";
import { InputLabel } from "./InputLabel";
import { ErrorMessage } from "./ErrorMessage";
import classNames from "classnames";
import React from "react";
import type { FieldErrors } from "react-hook-form";

interface CheckboxProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  errors?: FieldErrors;
}

export const Checkbox = React.forwardRef<HTMLInputElement, CheckboxProps>(
  ({ id, name, label, errors, className, disabled, ...rest }, ref) => {
    return (
      <InputLabel className={classNames(styles.Label, className)}>
        <input
          type="checkbox"
          id={id}
          name={name}
          className={classNames(
            styles.Checkbox,
            disabled ? styles.disabled : "",
          )}
          ref={ref}
          {...rest}
        />
        <div>
          {label}
          {name && errors?.[name] && (
            <ErrorMessage label-for={id}>
              {errors[name].message as string}
            </ErrorMessage>
          )}
        </div>
      </InputLabel>
    );
  },
);
