import React from "react";
import styles from "./Card.module.css";
import classNames from "classnames";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {}

export const Card: React.FC<CardProps> = ({ className, children, ...rest }) => (
  <div className={classNames(styles.Card, className)} {...rest}>
    {children}
  </div>
);
