import styles from "./InputLabel.module.css";
import type { Component } from "solid-js";
import { JSX } from "solid-js";
import classNames from "classnames";

interface InputLabelProps extends JSX.HTMLAttributes<HTMLLabelElement> {}

export const InputLabel: Component<InputLabelProps> = (props) => {
  return <label class={classNames(props.class ?? styles.InputLabel)} {...props}>{props.children}</label>;
};
