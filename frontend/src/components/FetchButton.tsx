import styles from "./FetchButton.module.css";
import type { Component, JSX } from "solid-js";
import { Switch, Match } from "solid-js";
import { Button } from "./Button";
import classNames from "classnames";
import { Spinner } from "./Spinner";

import errorIcon from "material-design-icons/alert/svg/production/ic_error_18px.svg";
import successIcon from "material-design-icons/action/svg/production/ic_check_circle_24px.svg";

const error = (status: number): boolean => status >= 400;
const success = (status: number): boolean => status < 400;

interface FetchButtonProps extends JSX.ButtonHTMLAttributes<HTMLButtonElement> {
  isFetching: boolean;
  status?: number;
}

export const FetchButton: Component<FetchButtonProps> = (props) => {
  return (
    <Button
      {...props}
      class={classNames(
        styles.FetchButton,
        props.isFetching && styles.isFetching,
        success(props.status) && styles.success,
        error(props.status) && styles.error,
        props.class
      )}
    >
      {props.children}
      <Switch>
        <Match when={success(props.status)}>
          <img class={styles.Icon} src={successIcon} alt="" />
        </Match>
        <Match when={error(props.status)}>
          <img class={styles.Icon} src={errorIcon} alt="" />
        </Match>
        <Match when={props.isFetching}>
          <Spinner />
        </Match>
      </Switch>
    </Button>
  );
};
