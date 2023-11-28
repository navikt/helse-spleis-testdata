import { ArbeidssituasjonDTO } from "../utils/types";

const isNumerical = (value?: string): boolean =>
  value !== undefined && !isNaN(Number(value));

const isInteger = (value?: string): boolean =>
  isNumerical(value) && Number(value) === Number.parseInt(value);

export const isNaturalNumber = (value?: string): boolean =>
  value !== undefined && value.match(/^\d+$/) !== null;

export const validateFødselsnummer = (value?: string): boolean | string =>
  !isNaturalNumber(value)
    ? "Fødselsnummeret må være et positivt heltall"
    : value?.length !== 11
    ? "Fødselsnummeret må bestå av 11 siffere"
    : true;

export const validateInntekt = (value: string): boolean | string =>
  isNumerical(value) || "Inntekten må være numerisk";

export const validateRefusjonsbeløp = (value?: string): boolean | string =>
  !value || isNumerical(value) || "Refusjonsbeløp må være numerisk";

const validateOrganisasjonsnummerNumerisk = (value: string): boolean | string =>
    value?.length !== 9
    ? "Organisasjonsnummeret må bestå av 9 siffere"
    : true;

const gyldigeArbeidssituasjoner = [ArbeidssituasjonDTO.Arbeidsledig.valueOf(), ArbeidssituasjonDTO.SelvstendigNæringsdrivende.valueOf(), ArbeidssituasjonDTO.Frilanser.valueOf()]
const validateOrganisasjonsnummerTekstlig = (value: string): boolean | string =>
    !gyldigeArbeidssituasjoner.includes(value)
    ? `Tekstlige orgnummere må være en av følgende verdier:${gyldigeArbeidssituasjoner}`
    : true

export const validateOrganisasjonsnummer = (value: string): boolean | string => {
    if (!isNaturalNumber(value)) {
        return validateOrganisasjonsnummerTekstlig(value)
    }
    return validateOrganisasjonsnummerNumerisk(value)
}

export const validateSykdomsgrad = (value?: string): boolean | string =>
  !isInteger(value)
    ? "Sykdomsgraden må være et heltall"
    : Number.parseInt(value) > 100
    ? "Sykdomsgrad må være 100 eller lavere"
    : Number.parseInt(value) < 0
    ? "Sykdomsgrad må være 0 eller høyere"
    : true;

export const validateArbeidsgrad = (value?: string): boolean | string =>
  value.length === 0 ||
  (!isInteger(value)
    ? "Arbeidsgraden må være et heltall"
    : Number.parseInt(value) > 100
    ? "Arbeidsgrad må være 100 eller lavere"
    : Number.parseInt(value) < 0
    ? "Arbeidsgrad må være 0 eller høyere"
    : true);

export const validateGruppeId = (value?: string): boolean | string =>
  isInteger(value) || "Gruppe-ID må være et heltall"