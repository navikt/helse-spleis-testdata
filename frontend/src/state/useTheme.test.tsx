import React, { act, ReactNode } from "react";
import { useTheme, useThemeState } from "./useTheme";
import { AppProvider } from "./AppContext";
import { describe, expect, it } from "vitest";
import { renderHook } from "@testing-library/react";

const wrapper = ({ children }: { children: ReactNode }) => (
  <AppProvider>{children}</AppProvider>
);

describe("useTheme", () => {
  it('returnerer theme og defaulter til "light"', () => {
    const { result } = renderHook(() => useTheme(), { wrapper });
    expect(result.current).toEqual("light");
  });
});

describe("useThemeState", () => {
  it("setter theme i localstorage ved endring", () => {
    const { result } = renderHook(() => useThemeState(), { wrapper });
    expect(localStorage.getItem("theme")).toBeNull();

    act(() => {
      result.current[1]("dark");
    });

    expect(localStorage.getItem("theme")).toEqual("dark");
    expect(result.current[0]).toEqual("dark");
  });
});
