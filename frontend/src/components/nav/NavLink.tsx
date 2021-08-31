import styles from "./NavLink.module.css";
import { NavLink as Link } from "react-router-dom";
import classNames from "classnames";
import React from "react";

interface NavLinkProps {
  to: string;
  isExpanded: boolean;
}

export const NavLink: React.FC<NavLinkProps> = (props) => {
  return (
    <li
      className={classNames(
        styles.Link,
        props.isExpanded ? styles.isExpanded : styles.isMinified
      )}
    >
      <Link to={props.to} exact={true}>
        {props.children}
      </Link>
    </li>
  );
};
