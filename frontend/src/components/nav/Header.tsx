import React, { PropsWithChildren } from "react";
import { Link, useMatch } from "react-router-dom";
import { FileTextIcon, PersonGroupIcon, WalletIcon } from "@navikt/aksel-icons";
import { Box, HStack, InternalHeader, Spacer } from "@navikt/ds-react";

import { ThemeButton } from "../ThemeButton";

import pumpkin from "../../assets/halloween/pumpkin.svg";
import styles from "./Header.module.css";

interface HeaderLinkProps {
  to: string;
  icon: React.ReactNode;
  emoji: string;
}

const HeaderLink: React.FC<PropsWithChildren<HeaderLinkProps>> = ({
  to,
  icon,
  emoji,
  children,
}) => {
  const isActive = useMatch({ path: to, end: true }) !== null;

  return (
    <InternalHeader.Button
      as={Link}
      to={to}
      isActive={isActive}
      aria-current={isActive ? "page" : undefined}
      className={styles.Link}
    >
      {icon}
      {children}
      <span aria-hidden className={styles.Emoji}>
        {emoji}
      </span>
    </InternalHeader.Button>
  );
};

export const Header = React.memo(() => (
  <InternalHeader>
    <InternalHeader.Title as="h1">
      <HStack as="span" gap="space-8" align="center" wrap={false}>
        <Box asChild width="1.75rem" height="1.75rem">
          <img src={pumpkin} alt="" className={styles.Logo} />
        </Box>
        Spleis testdata
        <span aria-hidden className={styles.Ghost}>
          👻
        </span>
      </HStack>
    </InternalHeader.Title>
    <HeaderLink
      to="/"
      emoji="📜"
      icon={<FileTextIcon aria-hidden fontSize="1.5rem" />}
    >
      Opprett dokumenter
    </HeaderLink>
    <HeaderLink
      to="/inntekt/hent"
      emoji="💰"
      icon={<WalletIcon aria-hidden fontSize="1.5rem" />}
    >
      Hent inntekt
    </HeaderLink>
    <HeaderLink
      to="/testgruppe"
      emoji="🧟"
      icon={<PersonGroupIcon aria-hidden fontSize="1.5rem" />}
    >
      Hent testgruppe
    </HeaderLink>
    <Spacer />
    <ThemeButton />
  </InternalHeader>
));

Header.displayName = "Header";
