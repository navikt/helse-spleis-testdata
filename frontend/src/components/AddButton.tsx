import React from "react";
import classNames from "classnames";
import styles from "./AddButton.module.css";

interface AddCardButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const AddButton: React.FC<AddCardButtonProps> = ({
  className,
  children,
  ...rest
}) => (
  <button
    className={classNames(styles.AddButton, className)}
    type="button"
    {...rest}
  >
    <i
      className={classNames(styles.Icon, "material-icons add_circle_outline")}
    />
    {children}
  </button>
);
