import { describe, expect, it } from "vitest";

import {
  fakturaKolonner,
  forsikringKolonner,
  manglerPåkrevdeFelter,
  nyForsikring,
  tilPayload,
  tilUtkast,
  visVerdi,
} from "./kolonner";
import { forsikringsfaktura, individuellForsikring } from "./testdata";

describe("tilUtkast", () => {
  it("gjør om raden til tekstverdier og tomme strenger for null", () => {
    const utkast = tilUtkast(
      forsikringKolonner,
      individuellForsikring({ id: 1, godkjent: false, opphørsdato: null }),
    );

    expect(utkast).toEqual({
      godkjent: "false",
      fom: "2026-01-01",
      virkningsdato: "2026-01-01",
      type: "SELVSTENDIG_80_PROSENT_FRA_DAG_1",
      premiegrunnlag: "0",
      opphørsdato: "",
      opphørsgrunn: "",
    });
    expect(utkast).not.toHaveProperty("id");
    expect(utkast).not.toHaveProperty("identitetsnummer");
  });
});

describe("tilPayload", () => {
  it("sender riktige typer og utelater id og identitetsnummer", () => {
    const payload = tilPayload(
      forsikringKolonner,
      tilUtkast(
        forsikringKolonner,
        individuellForsikring({
          id: 1,
          godkjent: false,
          premiegrunnlag: 500000,
          opphørsdato: "2026-12-31",
        }),
      ),
    );

    expect(payload).toEqual({
      godkjent: false,
      fom: "2026-01-01",
      virkningsdato: "2026-01-01",
      type: "SELVSTENDIG_80_PROSENT_FRA_DAG_1",
      premiegrunnlag: 500000,
      opphørsdato: "2026-12-31",
      opphørsgrunn: null,
    });
  });

  it("sender halvdel som tall og manglende betalingsdato som null", () => {
    const payload = tilPayload(
      fakturaKolonner,
      tilUtkast(fakturaKolonner, forsikringsfaktura({ id: 2, halvdel: 2 })),
    );

    expect(payload).toEqual({ år: 2026, halvdel: 2, betalingsdato: null });
  });

  it("tolker manglende avkrysning som usann", () => {
    expect(tilPayload(forsikringKolonner, {})).toMatchObject({
      godkjent: false,
    });
  });
});

describe("manglerPåkrevdeFelter", () => {
  it("krever år og halvdel på fakturaer", () => {
    expect(
      manglerPåkrevdeFelter(fakturaKolonner, {
        år: "2026",
        halvdel: "1",
        betalingsdato: "",
      }),
    ).toBe(false);
    expect(
      manglerPåkrevdeFelter(fakturaKolonner, { år: "", halvdel: "1" }),
    ).toBe(true);
    expect(
      manglerPåkrevdeFelter(fakturaKolonner, { år: "2026", halvdel: "" }),
    ).toBe(true);
  });

  it("krever ingen felter på forsikringer", () => {
    expect(manglerPåkrevdeFelter(forsikringKolonner, {})).toBe(false);
  });
});

describe("visVerdi", () => {
  const kolonne = (nøkkel: string) =>
    forsikringKolonner.find((it) => it.key === nøkkel)!;

  it("viser avkrysning for godkjent", () => {
    const rad = individuellForsikring({ id: 1 });
    expect(visVerdi(kolonne("godkjent"), rad)).toBe("✔️");
    expect(visVerdi(kolonne("godkjent"), { ...rad, godkjent: false })).toBe(
      "❌",
    );
  });

  it("viser lesbart navn for forsikringstypen", () => {
    expect(visVerdi(kolonne("type"), individuellForsikring({ id: 1 }))).toBe(
      "Selvstendig næringsdrivende 80 % fra 1. dag",
    );
  });

  it("viser tom tekst for manglende verdier", () => {
    expect(
      visVerdi(
        kolonne("opphørsdato"),
        individuellForsikring({ id: 1, opphørsdato: null }),
      ),
    ).toBe("");
  });
});

describe("nyForsikring", () => {
  it("foreslår en godkjent forsikring med en gyldig type", () => {
    const utkast = nyForsikring();

    expect(utkast.godkjent).toBe("true");
    expect(utkast.type).toBe("SELVSTENDIG_80_PROSENT_FRA_DAG_1");
    expect(utkast.fom).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });
});
