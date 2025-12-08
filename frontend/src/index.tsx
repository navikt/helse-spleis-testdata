import "./index.css";
import { App } from "./App";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { AppProvider } from "./state/AppContext";
import 'material-design-icons-iconfont/dist/material-design-icons.css'

const root = createRoot(document.getElementById("root")!);
root.render(
  <BrowserRouter>
    <AppProvider>
      <App />
    </AppProvider>
  </BrowserRouter>
);
