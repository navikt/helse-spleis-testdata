import React from "react";
import { render, screen } from "@testing-library/react";
import { CopyField } from "./CopyField";
import userEvent from "@testing-library/user-event";
import { describe, it, expect } from "vitest";

const mockNavigatorClipboard = (onWrite: (text: string) => void) => {
  Object.assign(navigator, {
    clipboard: {
      writeText: onWrite,
    },
  });
};

describe("CopyField", () => {
  it("kopierer verdien i input-feltet til utklippstavlen", async () => {
    let copiedValue: string;

    mockNavigatorClipboard((text: string) => (copiedValue = text));

    render(<CopyField label="En label" value="En verdi" />);
    await userEvent.click(screen.getByRole("button"));

    expect(copiedValue).toEqual("En verdi");
  });

  it("viser feilmelding om kopiering feilet", async () => {
    render(<CopyField label="En label" value="En verdi" />);
    await userEvent.click(screen.getByRole("button"));

    const feilmelding = "Kunne ikke kopiere til utklippstavle";
    const errorElement = screen.getByText(feilmelding);
    expect(errorElement).toBeVisible();
  });
});
