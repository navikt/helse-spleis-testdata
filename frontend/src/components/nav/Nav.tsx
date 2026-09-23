import React from "react";
import classNames from "classnames";
import { FileTextIcon, PersonGroupIcon, WalletIcon } from "@navikt/aksel-icons";
import { Heading } from "@navikt/ds-react";

import styles from "./Nav.module.css";
import { NavLink } from "./NavLink";
import { ExpandButton } from "./ExpandButton";
import { useLocalStorageState } from "../../state/persistance";

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
        <Heading level="1" size="medium" className={styles.Title}>
          <img className={styles.Logo} src={logo} alt="" />
          <span>Spleis testdata</span>
        </Heading>
        <NavLink to="/" isExpanded={isExpanded}>
          <FileTextIcon aria-hidden fontSize="1.5rem" />
          Opprett dokumenter
        </NavLink>
        <NavLink to="/inntekt/hent" isExpanded={isExpanded}>
          <WalletIcon aria-hidden fontSize="1.5rem" />
          Hent inntekt
        </NavLink>
        <NavLink to="/testgruppe" isExpanded={isExpanded}>
          <PersonGroupIcon aria-hidden fontSize="1.5rem" />
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
