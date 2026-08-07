import { ArbeidssituasjonDTO } from "../utils/types";

export interface PeriodeDTO {
  fom: string;
  tom: string;
}

export interface PersonDTO {
  fnr: string;
}

export interface FellesDTO {
  orgnummer: string;
  sykdomFom: string;
  sykdomTom: string;
  arbeidssituasjon: ArbeidssituasjonDTO;
}

export interface SykmeldingDTO {
  sykmeldingsgrad: number;
}

export interface SøknadDTO extends SykmeldingDTO {
  harAndreInntektskilder: boolean;
  ferieperioder: PeriodeDTO[];
  inntektFraNyttArbeidsforhold: InntektFraNyttArbeidsforholdDto[];
  egenmeldingsdagerFraSykmelding: string[];
  faktiskgrad: number;
  sendtNav?: string;
  sendtArbeidsgiver?: string;
  arbeidGjenopptatt?: string;
  tidligereArbeidsgiverOrgnummer?: string | null;
  inntektFraSigrun?: number | null;
  fraværFørSykmeldingen?: boolean | null;
  harBrukerOppgittForsikring?: boolean | null;
  meldingTilNavDagerFraSykmelding?: PeriodeDTO | null;
}

export interface ArbeidsgiveropplysningerDTO {
  inntekt: number;
  arbeidsgiverperiode: PeriodeDTO[];
  endringRefusjon: EndringIRefusjonDto[];
  opphørRefusjon?: string;
  refusjon: RefusjonDto;
  begrunnelseForReduksjonEllerIkkeUtbetalt: string;
  harOpphørAvNaturalytelser: boolean;
  vedtaksperiodeId: string;
  forespurt: boolean; // false == selvbestemt
  arsakTilInnsending: string | null; // Ny == forespurt, Endring == korrigerte, null = selvbestemt
}

export interface RefusjonDto {
  opphørRefusjon?: string;
  refusjonsbeløp?: number;
}

export interface InntektFraNyttArbeidsforholdDto {
  datoFom: string;
  datoTom?: string;
  belopPerDag: number;
  arbeidsstedOrgnummer: string;
}

export interface EndringIRefusjonDto {
  beløp: number;
  endringsdato: string;
}
