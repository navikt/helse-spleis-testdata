import styles from "./AddButton.module.css";

import addIcon from "material-design-icons/content/svg/production/ic_add_circle_outline_24px.svg";
import React from "react";

interface AddCardButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const AddButton: React.FC<AddCardButtonProps> = (props) => (
  <button className={styles.AddButton} type="button" {...props}>
    <img className={styles.Icon} src={addIcon} alt="" />
    {props.children}
  </button>
);
