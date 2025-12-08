import React from "react";
import { render, screen } from "@testing-library/react";
import { ThemeButton } from "./ThemeButton";
import { AppProvider } from "../state/AppContext";
import userEvent from "@testing-library/user-event";
import { describe, it, expect } from "vitest";

const wrapper = ({ children }) => <AppProvider>{children}</AppProvider>;

describe("ThemeButton", () => {
  it("toggler theme", async () => {
    render(<ThemeButton />, { wrapper });
    await userEvent.click(screen.getByRole("button"));
    expect(localStorage.getItem("theme")).toEqual("dark");

    await userEvent.click(screen.getByRole("button"));
    expect(localStorage.getItem("theme")).toEqual("light");
  });
});
