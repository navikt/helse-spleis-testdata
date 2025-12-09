import React from "react";
import { render, screen } from "@testing-library/react";
import { Checkbox } from "./Checkbox";
import { describe, expect, it } from "vitest";

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
        errors={{ checkbox: { type: "required", message: "En feilmelding" } }}
      />,
    );

    expect(screen.getByText("En feilmelding")).toBeVisible();
  });
});
