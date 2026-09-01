import {
  individuelleForsikringstyper,
  type Forsikringsfaktura,
  type IndividuellForsikring,
  type Valg,
} from "./typer";

export type KolonneType = "number" | "text" | "date" | "boolean" | "select";

export interface Kolonne<T> {
  key: Extract<keyof T, string>;
  tittel: string;
  type: KolonneType;
  påkrevd?: boolean;
  valg?: readonly Valg[];
}

export type Utkast = Record<string, string>;

const halvdeler: readonly Valg[] = [
  { verdi: "1", tekst: "1" },
  { verdi: "2", tekst: "2" },
];

export const forsikringKolonner: Kolonne<IndividuellForsikring>[] = [
  { key: "godkjent", tittel: "Godkjent", type: "boolean" },
  { key: "fom", tittel: "Fom", type: "date" },
  { key: "virkningsdato", tittel: "Virkningsdato", type: "date" },
  {
    key: "type",
    tittel: "Type",
    type: "select",
    valg: individuelleForsikringstyper,
  },
  { key: "premiegrunnlag", tittel: "Premiegrunnlag", type: "number" },
  { key: "opphørsdato", tittel: "Opphørsdato", type: "date" },
  { key: "opphørsgrunn", tittel: "Opphørsgrunn", type: "text" },
];

export const fakturaKolonner: Kolonne<Forsikringsfaktura>[] = [
  { key: "år", tittel: "År", type: "number", påkrevd: true },
  {
    key: "halvdel",
    tittel: "Halvdel",
    type: "select",
    valg: halvdeler,
    påkrevd: true,
  },
  { key: "betalingsdato", tittel: "Betalingsdato", type: "date" },
];

export const nyForsikring = (): Utkast => ({
  godkjent: "true",
  fom: iDag(),
  virkningsdato: iDag(),
  type: individuelleForsikringstyper[0].verdi,
  premiegrunnlag: "0",
  opphørsdato: "",
  opphørsgrunn: "",
});

export const nyFaktura = (): Utkast => ({
  år: String(new Date().getFullYear()),
  halvdel: "1",
  betalingsdato: "",
});

export const tilUtkast = <T>(kolonner: Kolonne<T>[], rad: T): Utkast =>
  Object.fromEntries(
    kolonner.map((kolonne) => {
      const verdi = rad[kolonne.key];
      return [
        kolonne.key,
        verdi === null || verdi === undefined ? "" : String(verdi),
      ];
    }),
  );

export const tilPayload = <T>(
  kolonner: Kolonne<T>[],
  utkast: Utkast,
): Record<string, unknown> => {
  const payload: Record<string, unknown> = {};
  kolonner.forEach((kolonne) => {
    const verdi = utkast[kolonne.key] ?? "";
    if (kolonne.type === "boolean") {
      payload[kolonne.key] = verdi === "true";
    } else if (verdi === "") {
      payload[kolonne.key] = null;
    } else if (kolonne.type === "number") {
      payload[kolonne.key] = Number(verdi);
    } else if (kolonne.type === "select") {
      payload[kolonne.key] = kolonne.valg?.every((valg) =>
        erHeltall(valg.verdi),
      )
        ? Number(verdi)
        : verdi;
    } else {
      payload[kolonne.key] = verdi;
    }
  });
  return payload;
};

export const manglerPåkrevdeFelter = <T>(
  kolonner: Kolonne<T>[],
  utkast: Utkast,
): boolean =>
  kolonner.some(
    (kolonne) => kolonne.påkrevd && (utkast[kolonne.key] ?? "").trim() === "",
  );

export const visVerdi = <T>(kolonne: Kolonne<T>, rad: T): string => {
  const verdi = rad[kolonne.key];
  if (kolonne.type === "boolean") return verdi ? "✔️" : "❌";
  if (verdi === null || verdi === undefined) return "";
  if (kolonne.type === "select")
    return (
      kolonne.valg?.find((valg) => valg.verdi === String(verdi))?.tekst ??
      String(verdi)
    );
  return String(verdi);
};

const erHeltall = (verdi: string): boolean => /^\d+$/.test(verdi);

const iDag = (): string => new Date().toISOString().slice(0, 10);
