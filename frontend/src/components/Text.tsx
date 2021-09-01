import React from "react";
import styles from "./Text.module.css";
import classNames from "classnames";

interface TextProps extends React.HTMLAttributes<HTMLParagraphElement> {}

export const Text: React.FC<TextProps> = ({ children, className, ...rest }) => (
  <p className={classNames(styles.Text, className)} {...rest}>
    {children}
  </p>
);
