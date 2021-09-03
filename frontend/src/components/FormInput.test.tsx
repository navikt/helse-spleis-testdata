import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { FormInput } from "./FormInput";

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
        errors={{ input: { message: "En feilmelding" } }}
      />
    );

    expect(screen.queryByText("En feilmelding")).toBeVisible();
  });
});
