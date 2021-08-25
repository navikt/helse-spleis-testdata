import styles from "./ErrorMessage.module.css";
import type { Component, JSX } from "solid-js";
import classNames from "classnames";

interface ErrorMessageProps extends JSX.HTMLAttributes<HTMLParagraphElement> {}

export const ErrorMessage: Component<ErrorMessageProps> = (props) => {
  return (
    <p class={classNames(styles.ErrorMessage, props.class)} {...props}>
      {props.children}
    </p>
  );
};
