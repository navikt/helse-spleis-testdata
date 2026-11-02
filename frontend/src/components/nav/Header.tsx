import React, { PropsWithChildren } from "react";
import { Link, useMatch } from "react-router-dom";
import { FileTextIcon, PersonGroupIcon, WalletIcon } from "@navikt/aksel-icons";
import { Box, HStack, InternalHeader, Spacer } from "@navikt/ds-react";

import { ThemeButton } from "../ThemeButton";

import logo from "../../assets/logo.png";

interface HeaderLinkProps {
  to: string;
  icon: React.ReactNode;
}

const HeaderLink: React.FC<PropsWithChildren<HeaderLinkProps>> = ({
  to,
  icon,
  children,
}) => {
  const isActive = useMatch({ path: to, end: true }) !== null;

  return (
    <InternalHeader.Button
      as={Link}
      to={to}
      isActive={isActive}
      aria-current={isActive ? "page" : undefined}
    >
      {icon}
      {children}
    </InternalHeader.Button>
  );
};

export const Header = React.memo(() => (
  <InternalHeader>
    <InternalHeader.Title as="h1">
      <HStack as="span" gap="space-8" align="center" wrap={false}>
        <Box asChild width="1.5rem" height="1.5rem">
          <img src={logo} alt="" />
        </Box>
        Spleis testdata
      </HStack>
    </InternalHeader.Title>
    <HeaderLink to="/" icon={<FileTextIcon aria-hidden fontSize="1.5rem" />}>
      Opprett dokumenter
    </HeaderLink>
    <HeaderLink
      to="/inntekt/hent"
      icon={<WalletIcon aria-hidden fontSize="1.5rem" />}
    >
      Hent inntekt
    </HeaderLink>
    <HeaderLink
      to="/testgruppe"
      icon={<PersonGroupIcon aria-hidden fontSize="1.5rem" />}
    >
      Hent testgruppe
    </HeaderLink>
    <Spacer />
    <ThemeButton />
  </InternalHeader>
));

Header.displayName = "Header";
