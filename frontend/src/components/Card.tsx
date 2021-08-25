import styles from "./Card.module.css";
import type { Component, JSX } from "solid-js";
import classNames from "classnames";

interface CardProps extends JSX.HTMLAttributes<HTMLDivElement> {}

export const Card: Component<CardProps> = (props) => {
  return (
    <div {...props} class={classNames(styles.Card, props.class)}>
      {props.children}
    </div>
  );
};
