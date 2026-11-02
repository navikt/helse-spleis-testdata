import React from "react";
import { Route, Routes } from "react-router-dom";
import { Theme } from "@navikt/ds-react";

import { useTheme } from "./state/useTheme";
import { HentInntekt } from "./screens/HentInntekt";
import { HentTestgruppe } from "./screens/HentTestgruppe";
import { OpprettDokumenter } from "./screens/opprettDokumenter/OpprettDokumenter";

import { Header } from "./components/nav/Header";
import { SystemMessages } from "./components/SystemMessages";

import styles from "./App.module.css";
import { AppStatus } from "./components/AppStatus";

export const App = () => {
  const theme = useTheme();

  return (
    <Theme theme={theme} hasBackground className={styles.App}>
      <Header />
      <main className={styles.Main}>
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
      </main>
      <SystemMessages />
    </Theme>
  );
};
