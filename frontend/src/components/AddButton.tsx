import styles from "./AddCardButton.module.css";
import type { Component, JSX } from "solid-js";

import addIcon from "material-design-icons/content/svg/production/ic_add_circle_outline_24px.svg";

interface AddCardButtonProps
  extends JSX.ButtonHTMLAttributes<HTMLButtonElement> {}

export const AddButton: Component<AddCardButtonProps> = (props) => {
  return (
    <button class={styles.AddButton} type="button" {...props}>
      <img class={styles.Icon} src={addIcon} alt="" />
      {props.children}
    </button>
  );
};
