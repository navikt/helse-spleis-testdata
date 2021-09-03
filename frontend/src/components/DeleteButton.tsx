import styles from "./DeleteButton.module.css";

import slettPersonIcon from "material-design-icons/action/svg/production/ic_delete_forever_24px.svg";
import React from "react";
import classNames from "classnames";

interface DeleteButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const DeleteButton: React.FC<DeleteButtonProps> = ({
  className,
  children,
  ...rest
}) => (
  <button className={classNames(styles.DeleteButton, className)} {...rest}>
    <img src={slettPersonIcon} alt="" />
    {children}
  </button>
);
