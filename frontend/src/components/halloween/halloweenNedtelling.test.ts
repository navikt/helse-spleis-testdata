import { describe, expect, it } from "vitest";
import { dagerTilHalloween, halloweenTekst } from "./halloweenNedtelling";

describe("dagerTilHalloween", () => {
  it("teller ned til årets Halloween", () => {
    expect(dagerTilHalloween(new Date(2026, 9, 27, 23, 59))).toBe(4);
  });

  it("er null på selve Halloween", () => {
    expect(dagerTilHalloween(new Date(2026, 9, 31, 12))).toBe(0);
  });

  it("teller ned til neste års Halloween etter at den er passert", () => {
    expect(dagerTilHalloween(new Date(2026, 10, 1))).toBe(364);
  });
});

describe("halloweenTekst", () => {
  it.each([
    [0, "Glad Halloween! Knask eller knep!"],
    [1, "Halloween er i morgen. Er kostymet klart?"],
    [4, "4 netter igjen til Halloween"],
  ])("gir riktig tekst for %i dager", (dager, tekst) => {
    expect(halloweenTekst(dager)).toBe(tekst);
  });
});
