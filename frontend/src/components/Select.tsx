import React from "react";
import styles from "./Input.module.css";
import classNames from "classnames";

interface Option {
  value: string;
  label: string;
}

interface SelectProps extends React.InputHTMLAttributes<HTMLSelectElement> {
  options: (string | Option)[];
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, options, ...rest }, ref) => {
    return (
      <select
        className={classNames(styles.Input, className)}
        ref={ref}
        {...rest}
      >
        {options.map((option, index) => {
          const label =
            typeof option === "object" ? (option as Option).label : option;
          const value =
            typeof option === "object" ? (option as Option).value : option;
          return <option value={value} key={index}>{label}</option>;
        })}
      </select>
    );
  }
);
