import React from "react";
import { act, renderHook } from "@testing-library/react-hooks";
import { useLocalStorageState } from "./persistance";

describe("useLocalStorageState", () => {
  const key = "key";

  it("lagrer verdi i localStorage", () => {
    const { result } = renderHook(() => useLocalStorageState(key));
    expect(JSON.parse(localStorage.getItem(key))).toBeNull();
    expect(result.current[0]).toBeNull();

    act(() => {
      result.current[1]("ny-verdi");
    });

    expect(JSON.parse(localStorage.getItem(key))).toEqual("ny-verdi");
    expect(result.current[0]).toEqual("ny-verdi");
  });

  it("fjerner verdi fra localStorage", () => {
    const { result } = renderHook(() => useLocalStorageState(key));

    act(() => {
      result.current[1]("ny-verdi");
    });

    expect(JSON.parse(localStorage.getItem(key))).toEqual("ny-verdi");
    expect(result.current[0]).toEqual("ny-verdi");

    act(() => {
      result.current[2]();
    });

    expect(JSON.parse(localStorage.getItem(key))).toBeNull();
    expect(result.current[0]).toEqual(null);
  });
});
