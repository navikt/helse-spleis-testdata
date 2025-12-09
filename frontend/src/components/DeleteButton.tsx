import styles from "./DeleteButton.module.css";

import React from "react";
import classNames from "classnames";

interface DeleteButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const DeleteButton: React.FC<DeleteButtonProps> = ({
  className,
  children,
  ...rest
}) => (
  <button className={classNames(styles.DeleteButton, className)} {...rest}>
    <i className="material-icons delete_forever" />
    {children}
  </button>
);
