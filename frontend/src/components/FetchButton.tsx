import React from "react";
import styles from "./FetchButton.module.css";
import classNames from "classnames";
import { Button } from "./Button";
import { Spinner } from "./Spinner";

import errorIcon from "material-design-icons/alert/svg/production/ic_error_18px.svg";
import successIcon from "material-design-icons/action/svg/production/ic_check_circle_24px.svg";

const error = (status: number): boolean => status >= 400;
const success = (status: number): boolean => status < 400;

interface FetchButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  isFetching: boolean;
  status?: number;
  className?: string;
}

export const FetchButton: React.FC<FetchButtonProps> = ({
  isFetching,
  status,
  className,
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
    {rest.children}
    {success(status) && (
      <img className={styles.Icon} src={successIcon} alt="" />
    )}
    {error(status) && <img className={styles.Icon} src={errorIcon} alt="" />}
    {isFetching && <Spinner />}
  </Button>
);
