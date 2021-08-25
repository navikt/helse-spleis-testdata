import styles from "./NavLink.module.css";
import type { Component } from "solid-js";
import { NavLink as Link } from "solid-app-router";
import classNames from "classnames";

interface NavLinkProps {
  to: string;
  isExpanded: boolean;
}

export const NavLink: Component<NavLinkProps> = (props) => {
  return (
    <li
      class={classNames(
        styles.Link,
        props.isExpanded ? styles.isExpanded : styles.isMinified
      )}
    >
      <Link href={props.to} end={true}>{props.children}</Link>
    </li>
  );
};
