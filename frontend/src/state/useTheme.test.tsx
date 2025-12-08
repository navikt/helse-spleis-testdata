import React, {act, ReactNode} from "react";
import {
  useTheme,
  useThemeState,
  useUpdateBodyBackgroundColor,
} from "./useTheme";
import { AppProvider } from "./AppContext";
import { describe, it, expect } from "vitest";
import {renderHook} from "@testing-library/react";

const wrapper = ({ children }: { children: ReactNode }) => <AppProvider>{children}</AppProvider>;

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

describe("useUpdateBodyBackgroundColor", () => {
  const propertyKey = "--body-background-color";

  it('setter css-property til "white" når theme er "light"', () => {
    renderHook(() => useUpdateBodyBackgroundColor("light"));
    const color = document.body.style.getPropertyValue(propertyKey);
    expect(color).toEqual("white");
  });

  it('setter css-property til "black" når theme er "dark"', () => {
    renderHook(() => useUpdateBodyBackgroundColor("dark"));
    const color = document.body.style.getPropertyValue(propertyKey);
    expect(color).toEqual("black");
  });
});
