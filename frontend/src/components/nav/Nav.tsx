import styles from "./Nav.module.css";
import { NavLink } from "./NavLink";
import opprettDokumenter from "material-design-icons/action/svg/production/ic_description_24px.svg";
import hentInntektIcon from "material-design-icons/editor/svg/production/ic_attach_money_24px.svg";
import hentAktørIdIcon from "material-design-icons/action/svg/production/ic_fingerprint_24px.svg";
import slettPersonIcon from "material-design-icons/action/svg/production/ic_delete_forever_24px.svg";
import classNames from "classnames";
import { ExpandButton } from "./ExpandButton";
import { useLocalStorageState } from "../../state/persistance";
import React from "react";

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
          <span>🧬</span>
          <span>Spleis testdata</span>
        </h1>
        <NavLink to="/" isExpanded={expanded}>
          <img src={opprettDokumenter} alt="" />
          Opprett dokumenter
        </NavLink>
        <NavLink to="/inntekt/hent" isExpanded={expanded}>
          <img src={hentInntektIcon} alt="" />
          "Hent inntekt
        </NavLink>
        <NavLink to="/aktorid/hent" isExpanded={expanded}>
          <img src={hentAktørIdIcon} alt="" />
          "Hent aktør-ID
        </NavLink>
        <NavLink to="/person/slett" isExpanded={expanded}>
          <img src={slettPersonIcon} alt="" />
          "Slett person
        </NavLink>
      </ul>
      <ExpandButton
        expanded={expanded}
        onExpand={() => setExpanded(!expanded)}
      />
    </nav>
  );
});
