import styles from "./Fade.module.css";
import type { Component } from "solid-js";
import { Transition } from "solid-transition-group";
import { JSX } from "solid-js";
import classNames from "classnames";

interface FadeProps extends JSX.HTMLAttributes<HTMLDivElement> {}

export const Fade: Component<FadeProps> = (props) => {
  return (
    <Transition name="fade" appear>
      <div class={classNames(styles.Fade, props.class)} {...props}>
        {props.children}
      </div>
    </Transition>
  );
};
