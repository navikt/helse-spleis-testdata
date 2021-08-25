import styles from "./CopyField.module.css";
import type { Component } from "solid-js";
import { createEffect, createSignal, Match, Show, Switch } from "solid-js";
import { Input } from "./Input";

import copyIcon from "material-design-icons/content/svg/production/ic_content_copy_24px.svg";
import copiedIcon from "material-design-icons/action/svg/production/ic_check_circle_24px.svg";
import errorIcon from "material-design-icons/alert/svg/production/ic_error_18px.svg";

import { Button } from "./Button";
import { ErrorMessage } from "./ErrorMessage";
import classNames from "classnames";

interface CopyFieldProps {
  label: string;
  value: string;
}

export const CopyField: Component<CopyFieldProps> = (props) => {
  const [error, setError] = createSignal(false);
  const [copied, setCopied] = createSignal(false);

  const copyValueToClipboard = () => {
    navigator.clipboard
      .writeText(props.value)
      .then(() => {
        setError(false);
        setCopied(true);
      })
      .catch(() => {
        setCopied(false);
        setError(true);
      });
  };

  createEffect(() => {
    props.value && setCopied(false);
  });

  return (
    <label class={styles.Label}>
      {props.label}
      <span class={styles.Flex}>
        <Input type="text" value={props.value} disabled />
        <Button
          type="button"
          class={classNames(
            styles.Button,
            copied() && styles.copied,
            error() && styles.error
          )}
          onClick={copyValueToClipboard}
        >
          <Switch fallback={<img src={copyIcon} alt="" />}>
            <Match when={copied()}>
              <img src={copiedIcon} alt="" />
            </Match>
            <Match when={error()}>
              <img src={errorIcon} alt="" />
            </Match>
          </Switch>
        </Button>
      </span>
      <Show when={error()}>
        <ErrorMessage>Kunne ikke kopiere til utklippstavle</ErrorMessage>
      </Show>
    </label>
  );
};
