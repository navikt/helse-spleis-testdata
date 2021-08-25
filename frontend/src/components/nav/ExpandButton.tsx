import styles from "./ExpandButton.module.css";
import type { Component, JSX } from "solid-js";

import classNames from "classnames";

interface ExpandButtonProps
  extends JSX.ButtonHTMLAttributes<HTMLButtonElement> {
  expanded: boolean;
  onExpand: () => void;
}

export const ExpandButton: Component<ExpandButtonProps> = (props) => {
  return (
    <button
      class={classNames(
        styles.ExpandButton,
        props.expanded ? styles.expanded : styles.minified
      )}
      {...props}
      onClick={props.onExpand}
    />
  );
};
