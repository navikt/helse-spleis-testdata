import styles from "./Input.module.css";
import type { Component, JSX } from "solid-js";
import classNames from "classnames";

interface InputProps extends JSX.InputHTMLAttributes<HTMLInputElement> {}

export const Input: Component<InputProps> = (props) => {
  return <input class={classNames(props.class ?? styles.Input)} {...props} />;
};
