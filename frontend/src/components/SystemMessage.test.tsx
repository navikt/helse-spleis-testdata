import React from "react";
import { render } from "@testing-library/react";
import "@testing-library/jest-dom";
import {
  SystemMessage,
  SystemMessageInitializationError,
} from "./SystemMessage";

describe("SystemMessage", () => {
  it("thrower dersom den ikke er lukkbar eller lukkes etter et oppgitt antall millisekunder", () => {
    expect(() =>
      render(<SystemMessage id="en-id" text="En melding" />)
    ).toThrow(SystemMessageInitializationError().message);
  });
});
