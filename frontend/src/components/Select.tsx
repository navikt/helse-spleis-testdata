import React, {ChangeEventHandler} from "react";
import styles from "./Input.module.css";
import classNames from "classnames";

interface Option {
    value: string
    label: string
}

interface SelectProps extends React.InputHTMLAttributes<HTMLSelectElement> {
    options: (string|Option)[]
}

export const Select = React.forwardRef<HTMLSelectElement, SelectProps>(
  ({ className, options, ...rest }, ref) => {
      console.log(`select`)
      console.log(rest)
      return (
        <select
              className={classNames(styles.Input, className)}
              ref={ref}
              {...rest}
          >
              { options.map(option => <option value={typeof option === 'object' ? (option as Option).value : option}>{typeof option === 'object' ? (option as Option).label : option}</option>)}
          </select>
    )
  }
);
