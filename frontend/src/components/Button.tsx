import styles from "./Button.module.css";
import type { Component, JSX } from "solid-js";
import classNames from "classnames";

interface ButtonProps extends JSX.ButtonHTMLAttributes<HTMLButtonElement> {}

export const Button: Component<ButtonProps> = (props) => {
  return (
    <button {...props} class={classNames(styles.Button, props.class)}>
      {props.children}
    </button>
  );
};
