import "./index.css";
import { App } from "./App";
import { render } from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { AppProvider } from "./state/AppContext";
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import React from "react";

render(
  <BrowserRouter>
    <AppProvider>
      <App />
    </AppProvider>
  </BrowserRouter>,
  document.getElementById("root")
);
