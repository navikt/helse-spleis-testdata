import { render } from "solid-js/web";
import { Router } from "solid-app-router";

import "./index.css";
import { App } from "./App";
import { ThemeProvider } from "./state/ThemeContext";
import { SystemMessageProvider } from "./state/SystemMessageContext";

render(
  () => (
    <Router>
      <SystemMessageProvider>
        <ThemeProvider>
          <App />
        </ThemeProvider>
      </SystemMessageProvider>
    </Router>
  ),
  document.getElementById("root")
);
