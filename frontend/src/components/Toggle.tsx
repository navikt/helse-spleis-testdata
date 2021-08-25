import styles from "./Toggle.module.css";
import type { Component, JSX } from "solid-js";
import { createSignal } from "solid-js";
import classNames from "classnames";

interface ToggleProps extends JSX.HTMLAttributes<HTMLButtonElement> {
  toggledByDefault?: boolean;
}

export const Toggle: Component<ToggleProps> = (props) => {
  const [toggled, setToggled] = createSignal(props.toggledByDefault ?? false);

  const onToggle = (event: Event) => {
    setToggled((old) => !old);
  };

  return (
    <button
      type="button"
      role="checkbox"
      aria-checked={toggled()}
      onClick={onToggle}
      class={classNames(styles.Toggle, toggled() && styles.toggled)}
    />
  );
};
