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
  harAndreInntektskilder: boolean;
  ferieperioder: PeriodeDTO[];
  faktiskgrad: number;
  sendtNav?: string;
  sendtArbeidsgiver?: string;
}

export interface InntektsmeldingDTO {
  inntekt: number;
  ferieperioder: PeriodeDTO[];
  arbeidsgiverperiode: PeriodeDTO[];
  endringRefusjon: string[];
  opphørRefusjon?: string;
  førsteFraværsdag?: string;
}
