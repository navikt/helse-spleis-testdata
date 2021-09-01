import React, { useEffect } from "react";
import { Route, Switch } from "react-router-dom";
import classNames from "classnames";
import { useTheme } from "./state/useTheme";
import { Nav } from "./components/nav/Nav";
import { ThemeButton } from "./components/ThemeButton";
import { HentInntekt } from "./screens/HentInntekt";
import { HentAktørId } from "./screens/HentAktørId";
import { SlettPerson } from "./screens/SlettPerson";
import { OpprettDokumenter } from "./screens/opprettDokumenter/OpprettDokumenter";
import styles from "./App.module.css";
import { SystemMessages } from "./components/SystemMessages";

const useUpdateBodyBackgroundColor = (theme) => {
  useEffect(() => {
    document.body.style.setProperty(
      "--body-background-color",
      theme === "light" ? "white" : "black"
    );
  }, [theme]);
};

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
      </Switch>
      <ThemeButton />
      <SystemMessages />
    </div>
  );
};
