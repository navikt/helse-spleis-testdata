import React from "react";
import { render, screen } from "@testing-library/react";
import "@testing-library/jest-dom/extend-expect";
import { ThemeButton } from "./ThemeButton";
import { RecoilRoot } from "recoil";
import userEvent from "@testing-library/user-event";

const wrapper = ({ children }) => <RecoilRoot>{children}</RecoilRoot>;

describe("ThemeButton", () => {
  it("toggler theme", async () => {
    render(<ThemeButton />, { wrapper });
    userEvent.click(screen.getByRole("button"));
    expect(localStorage.getItem("theme")).toEqual("dark");

    userEvent.click(screen.getByRole("button"));
    expect(localStorage.getItem("theme")).toEqual("light");
  });
});
