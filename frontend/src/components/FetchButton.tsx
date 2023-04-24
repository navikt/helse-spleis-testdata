import React from "react";
import styles from "./FetchButton.module.css";
import classNames from "classnames";
import { Button } from "./Button";
import { Spinner } from "./Spinner";

const error = (status?: number): boolean =>
  status !== undefined && status !== null && status >= 400;

const success = (status?: number): boolean =>
  status !== undefined && status !== null && status < 400;

interface FetchButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  isFetching: boolean;
  status?: number;
}

export const FetchButton: React.FC<FetchButtonProps> = ({
  isFetching,
  status,
  className,
  children,
  ...rest
}) => (
  <Button
    className={classNames(
      styles.FetchButton,
      isFetching && styles.isFetching,
      success(status) && styles.success,
      error(status) && styles.error,
      className
    )}
    {...rest}
  >
    {children}
    {success(status) && (
      <i
        className={classNames(styles.Icon, "material-icons check_circle")}
        data-testid="success"
      />
    )}
    {error(status) && (
      <i className={classNames(styles.Icon, "material-icons error")} data-testid="error" />
    )}
    {isFetching && <Spinner />}
  </Button>
);
