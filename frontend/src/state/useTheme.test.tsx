import React from "react";
import { act, renderHook } from "@testing-library/react-hooks";
import { useTheme, useThemeState } from "./useTheme";
import { RecoilRoot } from "recoil";

const wrapper = ({ children }) => <RecoilRoot>{children}</RecoilRoot>;

describe("useTheme", () => {
  test("setter theme i localstorage ved endring", () => {
    const { result } = renderHook(() => useThemeState(), { wrapper });
    expect(localStorage.getItem("theme")).toBeNull();

    act(() => {
      result.current[1]("dark");
    });

    expect(localStorage.getItem("theme")).toEqual("dark");
    expect(result.current[0]).toEqual("dark");
  });
});
