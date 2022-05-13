import React from "react";
import { Route, Switch } from "react-router-dom";
import classNames from "classnames";

import { useTheme, useUpdateBodyBackgroundColor } from "./state/useTheme";
import { HentInntekt } from "./screens/HentInntekt";
import { HentAktørId } from "./screens/HentAktørId";
import { SlettPerson } from "./screens/SlettPerson";
import { HentTestgruppe } from "./screens/HentTestgruppe";
import { OpprettDokumenter } from "./screens/opprettDokumenter/OpprettDokumenter";

import { Nav } from "./components/nav/Nav";
import { ThemeButton } from "./components/ThemeButton";
import { SystemMessages } from "./components/SystemMessages";

import styles from "./App.module.css";

export const App = () => {
  const theme = useTheme();

  useUpdateBodyBackgroundColor(theme);

  return (
    <div className={classNames(styles.App, styles[theme])}>
      <Nav />
      <Switch>
        <Route path="/" exact>
          <OpprettDokumenter />
        </Route>
        <Route path="/inntekt/hent">
          <HentInntekt />
        </Route>
        <Route path="/aktorid/hent">
          <HentAktørId />
        </Route>
        <Route path="/person/slett">
          <SlettPerson />
        </Route>
        <Route path="/testgruppe">
          <HentTestgruppe />
        </Route>
      </Switch>
      <ThemeButton />
      <SystemMessages />
    </div>
  );
};
