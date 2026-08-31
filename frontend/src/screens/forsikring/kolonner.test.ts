import { describe, expect, it } from "vitest";

import {
  faktiskFødselsnummer,
  sorterIfFkonto12,
  sorterIfVedfrivt10,
} from "./kolonner";
import { ifFkonto12, ifVedfrivt10 } from "./testdata";

describe("faktiskFødselsnummer", () => {
  it("bytter om dag og år", () => {
    expect(faktiskFødselsnummer(85_12_31_12345)).toBe("31128512345");
  });

  it("beholder ledende nuller", () => {
    expect(faktiskFødselsnummer(1_02_03_00123)).toBe("03020100123");
  });

  it("håndterer manglende verdi", () => {
    expect(faktiskFødselsnummer(null)).toBe("");
  });
});

describe("sorterIfVedfrivt10", () => {
  it("sorterer på fødselsnummer, så godkjenning, så virkdato, så forstom", () => {
    const rader = [
      ifVedfrivt10({
        ID_VED: 1,
        IF01_AGNR_FNR: 85_12_31_12345,
        IF10_GODKJ: "N",
      }),
      ifVedfrivt10({
        ID_VED: 2,
        IF01_AGNR_FNR: 85_12_31_12345,
        IF10_GODKJ: "J",
      }),
      ifVedfrivt10({ ID_VED: 3, IF01_AGNR_FNR: 85_01_01_12345 }),
      ifVedfrivt10({
        ID_VED: 4,
        IF01_AGNR_FNR: 85_12_31_12345,
        IF10_GODKJ: "J",
        IF10_VIRKDATO: 20250101,
      }),
      ifVedfrivt10({
        ID_VED: 5,
        IF01_AGNR_FNR: 85_12_31_12345,
        IF10_GODKJ: "J",
        IF10_FORSTOM: -1,
      }),
    ];

    expect(sorterIfVedfrivt10(rader).map((rad) => rad.ID_VED)).toEqual([
      3, 4, 5, 2, 1,
    ]);
  });

  it("endrer ikke lista som sendes inn", () => {
    const rader = [
      ifVedfrivt10({ ID_VED: 1, IF10_GODKJ: "N" }),
      ifVedfrivt10({ ID_VED: 2, IF10_GODKJ: "J" }),
    ];

    sorterIfVedfrivt10(rader);

    expect(rader.map((rad) => rad.ID_VED)).toEqual([1, 2]);
  });
});

describe("sorterIfFkonto12", () => {
  it("sorterer på IF12_FOM med tomme verdier sist", () => {
    const rader = [
      ifFkonto12({ ID_KONT: 1, IF12_FOM: null }),
      ifFkonto12({ ID_KONT: 2, IF12_FOM: 20260201 }),
      ifFkonto12({ ID_KONT: 3, IF12_FOM: 20260101 }),
    ];

    expect(sorterIfFkonto12(rader).map((rad) => rad.ID_KONT)).toEqual([
      3, 2, 1,
    ]);
  });
});
