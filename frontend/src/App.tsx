import React from "react";
import { Route, Routes } from "react-router-dom";
import classNames from "classnames";

import { useTheme, useUpdateBodyBackgroundColor } from "./state/useTheme";
import { HentInntekt } from "./screens/HentInntekt";
import { HentTestgruppe } from "./screens/HentTestgruppe";
import { OpprettDokumenter } from "./screens/opprettDokumenter/OpprettDokumenter";

import { Nav } from "./components/nav/Nav";
import { ThemeButton } from "./components/ThemeButton";
import { SystemMessages } from "./components/SystemMessages";

import styles from "./App.module.css";
import { AppStatus } from "./components/AppStatus";

export const App = () => {
  const theme = useTheme();

  useUpdateBodyBackgroundColor(theme);

  return (
    <div className={classNames(styles.App, styles[theme])}>
      <Nav />
      <Routes>
        <Route
          path="/"
          element={
            <div className={styles.OpprettDokumenterContainer}>
              <OpprettDokumenter />
              <AppStatus />
            </div>
          }
        />
        <Route path="/inntekt/hent" element={<HentInntekt />} />
        <Route path="/testgruppe" element={<HentTestgruppe />} />
      </Routes>
      <ThemeButton />
      <SystemMessages />
    </div>
  );
};
