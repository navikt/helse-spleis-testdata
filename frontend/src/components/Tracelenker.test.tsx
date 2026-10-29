import React from "react";
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { Tracelenker, traceUrlFra } from "./Tracelenker";

describe("traceUrlFra", () => {
  it("henter traceUrl fra svaret", () => {
    expect(
      traceUrlFra('{"traceUrl":"https://console.cloud.google.com/x"}'),
    ).toBe("https://console.cloud.google.com/x");
  });

  it("gir ingenting når svaret mangler traceUrl eller ikke er JSON", () => {
    expect(traceUrlFra('{"traceUrl":null}')).toBeUndefined();
    expect(traceUrlFra("")).toBeUndefined();
    expect(traceUrlFra("ikke json")).toBeUndefined();
    expect(traceUrlFra(undefined)).toBeUndefined();
  });
});

describe("Tracelenker", () => {
  it("viser ingenting uten lenker", () => {
    render(<Tracelenker lenker={[]} />);
    expect(screen.queryByTestId("tracelenker")).toBeNull();
  });

  it("viser lenkene i ny fane", () => {
    render(
      <Tracelenker
        lenker={[
          { tekst: "Dokumenter", url: "https://console.cloud.google.com/x" },
        ]}
      />,
    );
    const lenke = screen.getByRole("link", { name: /Dokumenter/ });
    expect(lenke).toHaveAttribute("href", "https://console.cloud.google.com/x");
    expect(lenke).toHaveAttribute("target", "_blank");
  });
});
