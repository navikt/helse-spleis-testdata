import styles from "./DeleteButton.module.css";

import slettPersonIcon from "material-design-icons/action/svg/production/ic_delete_forever_24px.svg";
import React from "react";

interface DeleteButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {}

export const DeleteButton: React.FC<DeleteButtonProps> = (props) => (
  <button className={styles.DeleteButton} {...props}>
    <img src={slettPersonIcon} alt="" />
    {props.children}
  </button>
);
