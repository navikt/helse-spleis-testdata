import React, { useState } from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { DatoFelt } from "./DatoFelt";

const Vertskomponent = ({
  startverdi,
  onEndret,
}: {
  startverdi: string;
  onEndret?: (isoDato: string) => void;
}) => {
  const [verdi, setVerdi] = useState(startverdi);
  return (
    <>
      <DatoFelt
        label="Dato"
        data-testid="dato"
        verdi={verdi}
        onEndret={(isoDato) => {
          setVerdi(isoDato);
          onEndret?.(isoDato);
        }}
      />
      <button type="button" onClick={() => setVerdi("2024-12-24")}>
        Sett utenfra
      </button>
    </>
  );
};

describe("DatoFelt", () => {
  it("viser en ISO-dato som dd.mm.yyyy", () => {
    render(<Vertskomponent startverdi="2021-07-01" />);

    expect(screen.getByTestId("dato")).toHaveValue("01.07.2021");
  });

  it("melder fra om datoen som ISO når brukeren skriver dd.mm.yyyy", () => {
    const onEndret = vi.fn();
    render(<Vertskomponent startverdi="" onEndret={onEndret} />);

    fireEvent.change(screen.getByTestId("dato"), {
      target: { value: "16.07.2021" },
    });

    expect(onEndret).toHaveBeenCalledWith("2021-07-16");
  });

  it("melder fra om tom verdi når teksten ikke er en gyldig dato", () => {
    const onEndret = vi.fn();
    render(<Vertskomponent startverdi="2021-07-01" onEndret={onEndret} />);

    fireEvent.change(screen.getByTestId("dato"), {
      target: { value: "16.07" },
    });

    expect(onEndret).toHaveBeenCalledWith("");
  });

  it("oppdaterer visningen når verdien endres utenfra", () => {
    render(<Vertskomponent startverdi="2021-07-01" />);

    fireEvent.click(screen.getByRole("button", { name: "Sett utenfra" }));

    expect(screen.getByTestId("dato")).toHaveValue("24.12.2024");
  });
});
