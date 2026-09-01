export interface Valg {
  verdi: string;
  tekst: string;
}

// Navnene er de samme som i sp-forsikring sin IndividuellForsikringType
export const individuelleForsikringstyper = [
  {
    verdi: "SELVSTENDIG_80_PROSENT_FRA_DAG_1",
    tekst: "Selvstendig næringsdrivende 80 % fra 1. dag",
  },
  {
    verdi: "SELVSTENDIG_100_PROSENT_FRA_DAG_17",
    tekst: "Selvstendig næringsdrivende 100 % fra 17. dag",
  },
  {
    verdi: "SELVSTENDIG_100_PROSENT_FRA_DAG_1",
    tekst: "Selvstendig næringsdrivende 100 % fra 1. dag",
  },
  {
    verdi: "SELVSTENDIG_JORDBRUKER_100_PROSENT_FRA_DAG_1",
    tekst: "Jordbruker tilleggsforsikring 100 % fra 1. dag",
  },
  {
    verdi: "FRILANSER_100_PROSENT_FRA_DAG_1",
    tekst: "Frilanser 100 % fra 1. dag",
  },
] as const satisfies readonly Valg[];

export type IndividuellForsikringstype =
  (typeof individuelleForsikringstyper)[number]["verdi"];

export interface IndividuellForsikring {
  id: number;
  identitetsnummer: string;
  godkjent: boolean;
  fom: string | null;
  virkningsdato: string | null;
  type: IndividuellForsikringstype | null;
  premiegrunnlag: number;
  opphørsdato: string | null;
  opphørsgrunn: string | null;
}

export interface Forsikringsfaktura {
  id: number;
  år: number | null;
  halvdel: number | null;
  betalingsdato: string | null;
}
