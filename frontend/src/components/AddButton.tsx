import React from "react";
import classNames from "classnames";
import styles from "./AddButton.module.css";
import addIcon from "material-design-icons/content/svg/production/ic_add_circle_outline_24px.svg";

interface AddCardButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

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
    <img className={styles.Icon} src={addIcon} alt="" />
    {children}
  </button>
);
