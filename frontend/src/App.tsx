import type { Component } from "solid-js";
import { createEffect, useContext } from "solid-js";
import { Route, Routes } from "solid-app-router";

import styles from "./App.module.css";
import { Nav } from "./components/nav/Nav";
import { ThemeContext } from "./state/ThemeProvider";
import classNames from "classnames";
import { ThemeButton } from "./components/ThemeButton";
import { OpprettDokumenter } from "./screens/opprettDokumenter/OpprettDokumenter";
import { HentInntekt } from "./screens/HentInntekt";
import { HentAktørId } from "./screens/HentAktørId";
import { SlettPerson } from "./screens/SlettPerson";

const useUpdateBodyBackgroundColor = (state) => {
  createEffect(() => {
    document.body.style.setProperty(
      "--body-background-color",
      state.theme === "light" ? "white" : "black"
    );
  });
};

export const App: Component = () => {
  const [state] = useContext(ThemeContext);

  useUpdateBodyBackgroundColor(state);

  return (
    <div class={classNames(styles.App, styles[state.theme])}>
      <Nav />
      <Routes>
        <Route path="/" element={<OpprettDokumenter />} />
        <Route path="/inntekt/hent" element={<HentInntekt />} />
        <Route path="/aktorid/hent" element={<HentAktørId />} />
        <Route path="/person/slett" element={<SlettPerson />} />
      </Routes>
      <ThemeButton />
    </div>
  );
};
