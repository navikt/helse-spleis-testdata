import React from "react";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { Header } from "./Header";
import { AppProvider } from "../../state/AppContext";

const renderAt = (path: string) =>
  render(
    <MemoryRouter initialEntries={[path]}>
      <AppProvider>
        <Header />
      </AppProvider>
    </MemoryRouter>,
  );

describe("Header", () => {
  it("markerer gjeldende side", () => {
    renderAt("/inntekt/hent");

    expect(screen.getByRole("link", { name: /Hent inntekt/ })).toHaveAttribute(
      "aria-current",
      "page",
    );
    expect(
      screen.getByRole("link", { name: /Opprett dokumenter/ }),
    ).not.toHaveAttribute("aria-current");
    expect(screen.getByRole("heading", { level: 1 })).toHaveTextContent(
      "Spleis testdata",
    );
    expect(
      screen.getByRole("button", { name: "Bytt til mørk modus" }),
    ).toBeInTheDocument();
  });

  it("markerer forsiden kun på rotstien", () => {
    renderAt("/");

    expect(
      screen.getByRole("link", { name: /Opprett dokumenter/ }),
    ).toHaveAttribute("aria-current", "page");
  });
});
