import "./index.css";
import { App } from "./App";
import { render } from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { RecoilRoot } from "recoil";
import 'material-design-icons-iconfont/dist/material-design-icons.css'
import React from "react";

render(
  <BrowserRouter>
    <RecoilRoot>
      <App />
    </RecoilRoot>
  </BrowserRouter>,
  document.getElementById("root")
);
