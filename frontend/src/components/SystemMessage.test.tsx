import React from "react";
import { render } from "@testing-library/react";
import {
  SystemMessage,
  SystemMessageInitializationError,
} from "./SystemMessage";
import { describe, expect, it } from "vitest";

describe("SystemMessage", () => {
  it("thrower dersom den ikke er lukkbar eller lukkes etter et oppgitt antall millisekunder", () => {
    expect(() =>
      render(<SystemMessage id="en-id" text="En melding" />),
    ).toThrow(SystemMessageInitializationError().message);
  });
});
