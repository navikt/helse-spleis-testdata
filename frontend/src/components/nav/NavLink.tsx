import styles from "./NavLink.module.css";
import { NavLink as Link } from "react-router-dom";
import classNames from "classnames";
import React from "react";

interface NavLinkProps {
  to: string;
  isExpanded: boolean;
}

export const NavLink: React.FC<NavLinkProps> = ({
  to,
  isExpanded,
  children,
}) => {
  return (
    <li
      className={classNames(
        styles.Link,
        isExpanded ? styles.isExpanded : styles.isMinified
      )}
    >
      <Link to={to} exact={true}>
        {children}
      </Link>
    </li>
  );
};
