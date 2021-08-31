import React from "react";
import styles from "./Card.module.css";
import classNames from "classnames";

interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  className?: string;
}

export const Card: React.FC<CardProps> = (props) => {
  return (
    <div {...props} className={classNames(styles.Card, props.className)}>
      {props.children}
    </div>
  );
};
