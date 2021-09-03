import styles from "./Button.module.css";
import classNames from "classnames";
import React from "react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const Button: React.FC<ButtonProps> = ({ className, ...rest }) => (
  <button className={classNames(styles.Button, className)} {...rest} />
);
