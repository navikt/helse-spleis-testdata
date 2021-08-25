import { render } from "solid-js/web";
import { Router } from "solid-app-router";

import "./index.css";
import { App } from "./App";
import { ThemeProvider } from "./state/ThemeProvider";

render(
  () => (
    <Router>
      <ThemeProvider>
        <App />
      </ThemeProvider>
    </Router>
  ),
  document.getElementById("root")
);
