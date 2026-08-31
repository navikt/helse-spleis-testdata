export interface IfVedfrivt10 {
  IF01_KODE: string;
  IF01_AGNR_FNR: number;
  IF10_FORSFOM_SEQ: number;
  IF10_GODKJ: string;
  IF10_FORSFOM: number;
  IF10_VIRKDATO: number;
  IF10_TYPE: string;
  IF10_SELVFOM: string;
  IF10_KOMBI: string;
  IF10_PREMGRL: number;
  IF10_FOM: number;
  IF10_PREMIE: number;
  IF10_GML_PREMGRL: number;
  IF10_GML_FOM: number;
  IF10_GML_PREMIE: number;
  IF10_FRIFOM: number;
  IF10_FORSTOM: number;
  IF10_OPPHGR: string;
  IF10_VARSEL: number;
  IF10_TERM_KV: string;
  IF10_TERM_AAR: string;
  IF10_VARSEL_BELOEP: number;
  IF10_BETALT_BELOEP: number;
  IF10_PURR: number;
  IF10_TKNR_BOST: number;
  IF10_TKNR_BEH: number;
  OPPRETTET: string;
  ENDRET_I_KILDE: string;
  KILDE_IF: string;
  ID_VED: number;
  OPPDATERT: string | null;
}

export interface IfFkonto12 {
  IF01_KODE: string | null;
  IF01_AGNR_FNR: number | null;
  IF10_FORSFOM_SEQ: number | null;
  IF12_BETDATO_SEQ: number | null;
  IF12_FOM: number | null;
  IF12_TOM: number | null;
  IF12_BET_KODE: string | null;
  IF12_FRIUKER: string | null;
  IF12_BELOEP: number | null;
  IF12_BETDATO: number | null;
  OPPRETTET: string;
  ENDRET_I_KILDE: string;
  KILDE_IF: string;
  ID_KONT: number;
  OPPDATERT: string | null;
}
