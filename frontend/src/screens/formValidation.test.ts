import {
  isNaturalNumber,
  validateArbeidsgrad,
  validateFødselsnummer,
  validateInntekt,
  validateOrganisasjonsnummer,
  validateSykdomsgrad,
} from "./formValidation";

describe("isNumerical", () => {
  it("returns false when input is not a natural number", () => {
    expect(isNaturalNumber("a123")).toEqual(false);
    expect(isNaturalNumber("abcd")).toEqual(false);
    expect(isNaturalNumber("10@$")).toEqual(false);
    expect(isNaturalNumber("-123")).toEqual(false);
    expect(isNaturalNumber("12.3")).toEqual(false);
    expect(isNaturalNumber("")).toEqual(false);
    expect(isNaturalNumber()).toEqual(false);
  });

  it("returns true when input is a natural number", () => {
    expect(isNaturalNumber("123423129642139182736")).toEqual(true);
    expect(isNaturalNumber("123")).toEqual(true);
    expect(isNaturalNumber("0")).toEqual(true);
  });
});

describe("validateFødselsnummer", () => {
  it("returns an error message when input is not a natural number", () => {
    const feilmelding = "Fødselsnummeret må være et positivt heltall";
    expect(validateFødselsnummer("a123")).toEqual(feilmelding);
    expect(validateFødselsnummer("-123")).toEqual(feilmelding);
    expect(validateFødselsnummer("abcd")).toEqual(feilmelding);
    expect(validateFødselsnummer()).toEqual(feilmelding);
  });

  it("returns an error message when input length is not 11", () => {
    const feilmelding = "Fødselsnummeret må bestå av 11 siffere";
    expect(validateFødselsnummer("123123123121")).toEqual(feilmelding);
    expect(validateFødselsnummer("1231231231")).toEqual(feilmelding);
  });

  it("returns when input consists of 11 digits", () => {
    expect(validateFødselsnummer("12312312312")).toEqual(true);
  });
});

describe("validateInntekt", () => {
  it("returns an error message when input is not a natural number", () => {
    const feilmelding = "Inntekten må være numerisk";
    expect(validateInntekt("abcd123456")).toEqual(feilmelding);
    expect(validateInntekt("litt-tekst")).toEqual(feilmelding);
    expect(validateInntekt("123456789a")).toEqual(feilmelding);
  });

  it("returns true when input is numerical", () => {
    expect(validateInntekt("1234567")).toEqual(true);
    expect(validateInntekt("123.123")).toEqual(true);
    expect(validateInntekt("-123456")).toEqual(true);
  });
});

describe("validateOrganisasjonsnummer", () => {
  it("ensures input is a natural number", () => {
    const feilmeldingNumerisk = "Organisasjonsnummeret må bestå av 9 siffere"
    const feilmeldingTekstlig = "Tekstlige orgnummere må være en av følgende verdier:ARBEIDSLEDIG,SELVSTENDIG_NARINGSDRIVENDE,FRILANSER"
    expect(validateOrganisasjonsnummer("123456789")).toEqual(true);
    expect(validateOrganisasjonsnummer("-123123123")).toEqual(feilmeldingTekstlig);
    expect(validateOrganisasjonsnummer("12345abcde")).toEqual(feilmeldingTekstlig);
    expect(validateOrganisasjonsnummer("12345.1234")).toEqual(feilmeldingTekstlig);
  });

  it("ensures input has 9 digits", () => {
    const feilmeldingNumerisk = "Organisasjonsnummeret må bestå av 9 siffere"
    expect(validateOrganisasjonsnummer("123456789")).toEqual(true);
    expect(validateOrganisasjonsnummer("12345678")).toEqual(feilmeldingNumerisk);
    expect(validateOrganisasjonsnummer("1234567890")).toEqual(feilmeldingNumerisk);
  });
});

describe("validateSykdomsgrad", () => {
  it("ensures input is a natural number", () => {
    const feilmelding = "Sykdomsgraden må være et heltall";
    expect(validateSykdomsgrad("0.1234")).toEqual(feilmelding);
    expect(validateSykdomsgrad("123abc")).toEqual(feilmelding);
    expect(validateSykdomsgrad("abcabc")).toEqual(feilmelding);
    expect(validateSykdomsgrad("50")).toEqual(true);
  });

  it("ensures input is 100 er less", () => {
    const feilmelding = "Sykdomsgrad må være 100 eller lavere";
    expect(validateSykdomsgrad("101")).toEqual(feilmelding);
    expect(validateSykdomsgrad("100")).toEqual(true);
  });

  it("ensures input is 0 er more", () => {
    const feilmelding = "Sykdomsgrad må være 0 eller høyere";
    expect(validateSykdomsgrad("-1")).toEqual(feilmelding);
    expect(validateSykdomsgrad("0")).toEqual(true);
  });
});

describe("validateArbeidsgrad", () => {
  it("ensures input is a natural number", () => {
    const feilmelding = "Arbeidsgraden må være et heltall";
    expect(validateArbeidsgrad("0.1234")).toEqual(feilmelding);
    expect(validateArbeidsgrad("123abc")).toEqual(feilmelding);
    expect(validateArbeidsgrad("abcabc")).toEqual(feilmelding);
    expect(validateArbeidsgrad("50")).toEqual(true);
  });

  it("ensures input is 100 er less", () => {
    const feilmelding = "Arbeidsgrad må være 100 eller lavere";
    expect(validateArbeidsgrad("101")).toEqual(feilmelding);
    expect(validateArbeidsgrad("100")).toEqual(true);
  });

  it("ensures input is 0 er more", () => {
    const feilmelding = "Arbeidsgrad må være 0 eller høyere";
    expect(validateArbeidsgrad("-1")).toEqual(feilmelding);
    expect(validateArbeidsgrad("0")).toEqual(true);
  });
});
