import "@navikt/ds-css";
import "./index.css";
import { App } from "./App";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AppProvider } from "./state/AppContext";

const root = createRoot(document.getElementById("root")!);
root.render(
  <BrowserRouter>
    <AppProvider>
      <App />
    </AppProvider>
  </BrowserRouter>,
);
