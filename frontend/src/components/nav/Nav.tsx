import styles from "./Nav.module.css";
import { NavLink } from "./NavLink";
import classNames from "classnames";
import { ExpandButton } from "./ExpandButton";
import { useLocalStorageState } from "../../state/persistance";
import React from "react";

import logo from "../../assets/logo.png";

export const Nav = React.memo(() => {
  const [expanded, setExpanded] = useLocalStorageState<boolean>("expanded");
  const isExpanded = expanded ?? false;

  return (
    <nav
      className={classNames(
        styles.Navigation,
        isExpanded ? styles.isExpanded : styles.isMinified,
      )}
      aria-expanded={isExpanded}
    >
      <ul className={styles.Links}>
        <h1 className={styles.Title}>
          <img className={styles.Logo} src={logo} alt="" />
          <span>Spleis testdata</span>
        </h1>
        <NavLink to="/" isExpanded={isExpanded}>
          <i className={classNames("material-icons", "description")} />
          Opprett dokumenter
        </NavLink>
        <NavLink to="/inntekt/hent" isExpanded={isExpanded}>
          <i className="material-icons attach_money" />
          Hent inntekt
        </NavLink>
        <NavLink to="/testgruppe" isExpanded={isExpanded}>
          <i className="material-icons accessibility" />
          Hent testgruppe
        </NavLink>
      </ul>
      <ExpandButton
        expanded={isExpanded}
        onExpand={() => setExpanded(!isExpanded)}
      />
    </nav>
  );
});
