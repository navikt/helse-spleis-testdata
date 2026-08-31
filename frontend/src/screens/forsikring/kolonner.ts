import type { IfFkonto12, IfVedfrivt10 } from "./typer";

export type KolonneType = "number" | "text";

export interface Kolonne<T> {
  key: Extract<keyof T, string>;
  type: KolonneType;
  readOnly?: boolean;
}

export const ifVedfrivt10Kolonner: Kolonne<IfVedfrivt10>[] = [
  { key: "ID_VED", type: "number", readOnly: true },
  { key: "IF01_KODE", type: "text" },
  { key: "IF01_AGNR_FNR", type: "number" },
  { key: "IF10_FORSFOM_SEQ", type: "number" },
  { key: "IF10_GODKJ", type: "text" },
  { key: "IF10_FORSFOM", type: "number" },
  { key: "IF10_VIRKDATO", type: "number" },
  { key: "IF10_TYPE", type: "text" },
  { key: "IF10_SELVFOM", type: "text" },
  { key: "IF10_KOMBI", type: "text" },
  { key: "IF10_PREMGRL", type: "number" },
  { key: "IF10_FOM", type: "number" },
  { key: "IF10_PREMIE", type: "number" },
  { key: "IF10_GML_PREMGRL", type: "number" },
  { key: "IF10_GML_FOM", type: "number" },
  { key: "IF10_GML_PREMIE", type: "number" },
  { key: "IF10_FRIFOM", type: "number" },
  { key: "IF10_FORSTOM", type: "number" },
  { key: "IF10_OPPHGR", type: "text" },
  { key: "IF10_VARSEL", type: "number" },
  { key: "IF10_TERM_KV", type: "text" },
  { key: "IF10_TERM_AAR", type: "text" },
  { key: "IF10_VARSEL_BELOEP", type: "number" },
  { key: "IF10_BETALT_BELOEP", type: "number" },
  { key: "IF10_PURR", type: "number" },
  { key: "IF10_TKNR_BOST", type: "number" },
  { key: "IF10_TKNR_BEH", type: "number" },
  { key: "OPPRETTET", type: "text" },
  { key: "ENDRET_I_KILDE", type: "text" },
  { key: "KILDE_IF", type: "text" },
  { key: "OPPDATERT", type: "text" },
];

export const ifFkonto12Kolonner: Kolonne<IfFkonto12>[] = [
  { key: "ID_KONT", type: "number", readOnly: true },
  { key: "IF01_KODE", type: "text" },
  { key: "IF01_AGNR_FNR", type: "number" },
  { key: "IF10_FORSFOM_SEQ", type: "number" },
  { key: "IF12_BETDATO_SEQ", type: "number" },
  { key: "IF12_FOM", type: "number" },
  { key: "IF12_TOM", type: "number" },
  { key: "IF12_BET_KODE", type: "text" },
  { key: "IF12_FRIUKER", type: "text" },
  { key: "IF12_BELOEP", type: "number" },
  { key: "IF12_BETDATO", type: "number" },
  { key: "OPPRETTET", type: "text" },
  { key: "ENDRET_I_KILDE", type: "text" },
  { key: "KILDE_IF", type: "text" },
  { key: "OPPDATERT", type: "text" },
];

export const nyIfVedfrivt10 = (): Record<string, string> => {
  const nå = new Date().toISOString();
  return {
    IF01_KODE: "1",
    IF01_AGNR_FNR: "0",
    IF10_FORSFOM_SEQ: "0",
    IF10_GODKJ: "J",
    IF10_FORSFOM: "0",
    IF10_VIRKDATO: "0",
    IF10_TYPE: "1",
    IF10_SELVFOM: " ",
    IF10_KOMBI: " ",
    IF10_PREMGRL: "0",
    IF10_FOM: "0",
    IF10_PREMIE: "0",
    IF10_GML_PREMGRL: "0",
    IF10_GML_FOM: "0",
    IF10_GML_PREMIE: "0",
    IF10_FRIFOM: "0",
    IF10_FORSTOM: "0",
    IF10_OPPHGR: " ",
    IF10_VARSEL: "0",
    IF10_TERM_KV: " ",
    IF10_TERM_AAR: " ",
    IF10_VARSEL_BELOEP: "0",
    IF10_BETALT_BELOEP: "0",
    IF10_PURR: "0",
    IF10_TKNR_BOST: "0",
    IF10_TKNR_BEH: "0",
    OPPRETTET: nå,
    ENDRET_I_KILDE: nå,
    KILDE_IF: " ",
    OPPDATERT: "",
  };
};

export const nyIfFkonto12 = (
  forelder: IfVedfrivt10 | null,
): Record<string, string> => {
  const nå = new Date().toISOString();
  return {
    IF01_KODE: forelder?.IF01_KODE ?? "1",
    IF01_AGNR_FNR: forelder ? String(forelder.IF01_AGNR_FNR) : "0",
    IF10_FORSFOM_SEQ: forelder ? String(forelder.IF10_FORSFOM_SEQ) : "0",
    IF12_BETDATO_SEQ: "0",
    IF12_FOM: "0",
    IF12_TOM: "0",
    IF12_BET_KODE: " ",
    IF12_FRIUKER: " ",
    IF12_BELOEP: "0",
    IF12_BETDATO: "0",
    OPPRETTET: nå,
    ENDRET_I_KILDE: nå,
    KILDE_IF: " ",
    OPPDATERT: "",
  };
};

/**
 * IF01_AGNR_FNR lagres med dag og år byttet om, altså som ÅÅMMDDPPPPP.
 * Her settes verdien tilbake til et faktisk fødselsnummer, DDMMÅÅPPPPP.
 */
export const faktiskFødselsnummer = (IF01_AGNR_FNR: number | null): string => {
  if (IF01_AGNR_FNR === null || IF01_AGNR_FNR === undefined) return "";
  const lagret = String(IF01_AGNR_FNR).padStart(11, "0");
  const år = lagret.slice(0, 2);
  const måned = lagret.slice(2, 4);
  const dag = lagret.slice(4, 6);
  return `${dag}${måned}${år}${lagret.slice(6)}`;
};

const godkjentRekkefølge = (IF10_GODKJ: string): number => {
  if (IF10_GODKJ === "J") return 0;
  if (IF10_GODKJ === "N") return 1;
  return 2;
};

const sammenlign = (a: number | string, b: number | string): number =>
  a < b ? -1 : a > b ? 1 : 0;

export const sorterIfVedfrivt10 = (rader: IfVedfrivt10[]): IfVedfrivt10[] =>
  [...rader].sort(
    (a, b) =>
      sammenlign(
        faktiskFødselsnummer(a.IF01_AGNR_FNR),
        faktiskFødselsnummer(b.IF01_AGNR_FNR),
      ) ||
      sammenlign(
        godkjentRekkefølge(a.IF10_GODKJ),
        godkjentRekkefølge(b.IF10_GODKJ),
      ) ||
      sammenlign(a.IF10_GODKJ, b.IF10_GODKJ) ||
      sammenlign(a.IF10_VIRKDATO, b.IF10_VIRKDATO) ||
      sammenlign(a.IF10_FORSTOM, b.IF10_FORSTOM),
  );

export const sorterIfFkonto12 = (rader: IfFkonto12[]): IfFkonto12[] =>
  [...rader].sort((a, b) =>
    sammenlign(
      a.IF12_FOM ?? Number.MAX_SAFE_INTEGER,
      b.IF12_FOM ?? Number.MAX_SAFE_INTEGER,
    ),
  );
