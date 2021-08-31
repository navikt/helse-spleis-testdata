import styles from "./ErrorMessage.module.css";
import React from "react";
import classNames from "classnames";

interface ErrorMessageProps extends React.HTMLAttributes<HTMLParagraphElement> {
  className?: string;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({
  className,
  ...rest
}) => <p className={classNames(styles.ErrorMessage, className)} {...rest} />;
