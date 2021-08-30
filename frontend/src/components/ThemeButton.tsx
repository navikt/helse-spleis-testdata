import styles from "./ThemeButton.module.css";
import type { Component } from "solid-js";
import { Match, Switch, useContext } from "solid-js";
import { ThemeContext } from "../state/ThemeContext";

import lightIcon from "material-design-icons/image/svg/production/ic_wb_sunny_24px.svg";
import darkIcon from "material-design-icons/image/svg/production/ic_brightness_2_24px.svg";
import classNames from "classnames";

export const ThemeButton: Component = () => {
  const [state, { setTheme }] = useContext(ThemeContext);

  const toggleTheme = () => {
    setTheme(state.theme === "light" ? "dark" : "light");
  };

  return (
    <button
      class={classNames(styles.ThemeButton, styles[state.theme])}
      onClick={toggleTheme}
    >
      <Switch fallback={<div />}>
        <Match when={state.theme === "light"}>
          <img src={lightIcon} alt="" />
        </Match>
        <Match when={state.theme === "dark"}>
          <img src={darkIcon} alt="" />
        </Match>
      </Switch>
    </button>
  );
};
