export const nonNumerical = (value: string, message: string): false | string =>
  isNaN(Number.parseInt(value)) && message;

export const invalidFnr = (value: string): false | string =>
  nonNumerical(value, "Fødselsnummeret må være numerisk") ||
  (value.length !== 11 && "Fødselsnummeret må bestå av 11 siffere");

export const invalidOrganisasjonsnummer = (value: string): false | string =>
  nonNumerical(value, "Organisasjonsnummeret må være numerisk");

export const invalidSykdomsgrad = (value: string): false | string =>
  nonNumerical(value, "Sykdomsgraden må være numerisk") ||
  (Number.parseInt(value) > 100 && "Sykdomsgrad må være 100 eller lavere") ||
  (Number.parseInt(value) < 0 && "Sykdomsgrad må være 0 eller høyere");

export const invalidArbeidsgrad = (value: string): false | string =>
  nonNumerical(value, "Arbeidsgraden må være numerisk") ||
  (Number.parseInt(value) > 100 && "Arbeidsgrad må være 100 eller lavere") ||
  (Number.parseInt(value) < 0 && "Arbeidsgrad må være 0 eller høyere");