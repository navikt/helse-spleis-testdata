import React from "react";
import styles from "./Input.module.css";
import classNames from "classnames";

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, ...rest }, ref) => (
    <input
      className={classNames(styles.Input, className)}
      {...rest}
      ref={ref}
    />
  )
);
