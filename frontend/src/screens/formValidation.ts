const isNumerical = (value?: string): boolean => value?.match(/^\d+$/) !== null;

export const validateFødselsnummer = (value?: string): boolean | string =>
  !isNumerical(value)
    ? "Fødselsnummeret må være numerisk"
    : value?.length !== 11
    ? "Fødselsnummeret må bestå av 11 siffere"
    : true;

export const validateInntekt = (value: string): boolean | string =>
  isNumerical(value) || "Inntekten må være numerisk";

export const validateOrganisasjonsnummer = (value: string): boolean | string =>
  isNumerical(value) || "Organisasjonsnummeret må være numerisk";

export const validateSykdomsgrad = (value?: string): boolean | string =>
  !isNumerical(value)
    ? "Sykdomsgraden må være numerisk"
    : Number.parseInt(value) > 100
    ? "Sykdomsgrad må være 100 eller lavere"
    : Number.parseInt(value) < 0
    ? "Sykdomsgrad må være 0 eller høyere"
    : true;

export const validateArbeidsgrad = (value?: string): boolean | string =>
  value.length === 0 ||
  (!isNumerical(value)
    ? "Arbeidsgraden må være numerisk"
    : Number.parseInt(value) > 100
    ? "Arbeidsgrad må være 100 eller lavere"
    : Number.parseInt(value) < 0
    ? "Arbeidsgrad må være 0 eller høyere"
    : true);
