import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { Checkbox } from "./Checkbox";

describe("Checkbox", () => {
  it("viser ikke feilmelding når det ikke finnes feil", () => {
    render(<Checkbox label="En label" name="checkbox" id="checkbox" />);

    expect(screen.queryByText("En feilmelding")).toBeNull();
  });

  it("viser feilmelding når det finnes feil", () => {
    render(
      <Checkbox
        label="En label"
        name="checkbox"
        id="checkbox"
        errors={{ checkbox: { message: "En feilmelding" } }}
      />
    );

    expect(screen.getByText("En feilmelding")).toBeVisible();
  });
});
