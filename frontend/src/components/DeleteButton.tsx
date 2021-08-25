import styles from "./DeleteButton.module.css";
import type { Component, JSX } from "solid-js";

import slettPersonIcon from "material-design-icons/action/svg/production/ic_delete_forever_24px.svg";

interface DeleteButtonProps
  extends JSX.ButtonHTMLAttributes<HTMLButtonElement> {}

export const DeleteButton: Component<DeleteButtonProps> = (props) => {
  return (
    <button class={styles.DeleteButton} {...props}>
      <img src={slettPersonIcon} alt="" />
      {props.children}
    </button>
  );
};
