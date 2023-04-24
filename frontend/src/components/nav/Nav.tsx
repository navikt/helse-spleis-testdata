import styles from "./Nav.module.css";
import { NavLink } from "./NavLink";
import classNames from "classnames";
import { ExpandButton } from "./ExpandButton";
import { useLocalStorageState } from "../../state/persistance";
import React from "react";

import logo from "../../assets/logo.png";

export const Nav = React.memo(() => {
  const [expanded, setExpanded] = useLocalStorageState<boolean>("expanded");

  return (
    <nav
      className={classNames(
        styles.Navigation,
        expanded ? styles.isExpanded : styles.isMinified
      )}
      aria-expanded={expanded}
    >
      <ul className={styles.Links}>
        <h1 className={styles.Title}>
          <img className={styles.Logo} src={logo} alt="" />
          <span>Spleis testdata</span>
        </h1>
        <NavLink to="/" isExpanded={expanded}>
          <i className={classNames("material-icons", "description")} />
          Opprett dokumenter
        </NavLink>
        <NavLink to="/inntekt/hent" isExpanded={expanded}>
          <i className="material-icons attach_money" />
          Hent inntekt
        </NavLink>
        <NavLink to="/aktorid/hent" isExpanded={expanded}>
          <i className="material-icons fingerprint" />
          Hent aktør-ID
        </NavLink>
        <NavLink to="/person/slett" isExpanded={expanded}>
          <i className="material-icons delete_forever" />
          Slett person
        </NavLink>
        <NavLink to="/testgruppe" isExpanded={expanded}>
          <i className="material-icons accessibility" />
          Hent testgruppe
        </NavLink>
      </ul>
      <ExpandButton
        expanded={expanded}
        onExpand={() => setExpanded(!expanded)}
      />
    </nav>
  );
});
