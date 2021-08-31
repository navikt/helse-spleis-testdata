import styles from "./Button.module.css";
import classNames from "classnames";
import React from "react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  className?: string;
}

export const Button: React.FC<ButtonProps> = ({
  className,
  ...buttonProps
}) => (
  <button className={classNames(styles.Button, className)} {...buttonProps} />
);
