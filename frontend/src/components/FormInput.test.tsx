import React from "react";
import { render, screen } from "@testing-library/react";
import { FormInput } from "./FormInput";
import { describe, it, expect } from "vitest";

describe("FormInput", () => {
  it("viser ikke feilmelding når det ikke finnes feil", () => {
    render(<FormInput label="En label" name="input" />);

    expect(screen.queryByText("En feilmelding")).toBeNull();
  });

  it("viser feilmelding ved feil", () => {
    render(
      <FormInput
        label="En label"
        name="input"
        errors={{ input: { type: "required", message: "En feilmelding" } }}
      />
    );

    expect(screen.queryByText("En feilmelding")).toBeVisible();
  });
});
