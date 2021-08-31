import styles from "./InputLabel.module.css";
import classNames from "classnames";
import React from "react";

interface InputLabelProps extends React.HTMLAttributes<HTMLLabelElement> {
  className?: string;
}

export const InputLabel: React.FC<InputLabelProps> = ({
  className,
  ...rest
}) => (
  <label className={classNames(className ?? styles.InputLabel)} {...rest} />
);
