import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom";
import { CopyField } from "./CopyField";
import { click } from "@testing-library/user-event/dist/click";

const mockNavigatorClipboard = (onWrite: (text: string) => void) => {
  Object.assign(navigator, {
    clipboard: {
      writeText: onWrite,
    },
  });
};

describe("CopyField", () => {
  it("kopierer verdien i input-feltet til utklippstavlen", () => {
    let copiedValue: string;

    mockNavigatorClipboard((text: string) => (copiedValue = text));

    render(<CopyField label="En label" value="En verdi" />);
    click(screen.getByRole("button"));

    expect(copiedValue).toEqual("En verdi");
  });

  it("viser feilmelding om kopiering feilet", () => {
    render(<CopyField label="En label" value="En verdi" />);
    click(screen.getByRole("button"));

    const feilmelding = "Kunne ikke kopiere til utklippstavle";
    const errorElement = screen.getByText(feilmelding);
    expect(errorElement).toBeVisible();
  });
});
