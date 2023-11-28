import {ArbeidssituasjonDTO} from "../utils/types";

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
}

export interface SykmeldingDTO {
  sykmeldingsgrad: number;
}

export interface SøknadDTO extends SykmeldingDTO {
  arbeidssituasjon: ArbeidssituasjonDTO,
  harAndreInntektskilder: boolean;
  ferieperioder: PeriodeDTO[];
  egenmeldingsdagerFraSykmelding: string[];
  faktiskgrad: number;
  sendtNav?: string;
  sendtArbeidsgiver?: string;
  arbeidGjenopptatt?: string;
}

export interface InntektsmeldingDTO {
  inntekt: number;
  arbeidsgiverperiode: PeriodeDTO[];
  endringRefusjon: EndringIRefusjonDto[];
  opphørRefusjon?: string;
  førsteFraværsdag?: string;
  refusjon: RefusjonDto;
  begrunnelseForReduksjonEllerIkkeUtbetalt: string;
  harOpphørAvNaturalytelser: boolean
}

export interface RefusjonDto {
  opphørRefusjon?: string;
  refusjonsbeløp?: number;
}

export interface EndringIRefusjonDto {
  beløp: number;
  endringsdato: string;
}