import React, {ReactNode} from "react";
import {act, fireEvent, render, screen, waitFor,} from "@testing-library/react";
import {OpprettDokumenter} from "./OpprettDokumenter";
import {AppProvider} from "../../state/AppContext";
import userEvent from "@testing-library/user-event";
import {beforeEach, describe, expect, it, Mock, vi} from "vitest";

const { subscribeMock } = vi.hoisted(() => ({
  subscribeMock: vi.fn(
    (
      _fødselsnummer: string,
      _callback: (
        vedtaksperiodeId: string,
        organisasjonsnummer: string,
      ) => void,
    ) => {},
  ),
}));

vi.mock("../../io/subscription", () => ({
  useSubscribe: () => [subscribeMock],
}));

vi.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = vi.fn();

const køetteSvar: object[] = [];

// Køer opp neste svar. Kall til de individuelle forsikringene besvares utenom køen,
// slik at rekkefølgen i de øvrige testene ikke påvirkes av forsikringsseksjonen.
const mockFetchResponse = (body: object) => {
  køetteSvar.push(body);
};

const wrapper = ({ children }: { children: ReactNode }) => (
  <AppProvider>{children}</AppProvider>
);

// Datofeltene viser dd.mm.yyyy, mens skjemaet og API-et bruker ISO.
const skrivDato = (testId: string, isoDato: string) => {
  const [år, måned, dag] = isoDato.split("-");
  fireEvent.change(screen.getByTestId(testId), {
    target: { value: `${dag}.${måned}.${år}` },
  });
};

const visDato = (isoDato: string): string => {
  const [år, måned, dag] = isoDato.split("-");
  return `${dag}.${måned}.${år}`;
};

describe("OpprettDokumenter", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    køetteSvar.length = 0;
    (fetch as Mock).mockImplementation((url: string) => {
      if (url.includes("individuelle-forsikringer"))
        return Promise.resolve({ ok: true, status: 200, json: () => [] });
      return Promise.resolve(
        køetteSvar.shift() ?? { status: 200, json: () => ({}), text: () => "" },
      );
    });
  });

  it("oppretter dokumenter", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    const inntekt = "54321";
    const fnr = "01234567890";

    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, inntekt);
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), fnr);
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        inntekt,
      ),
    );
    mockFetchResponse({ status: 200, text: () => vi.fn() });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    await new Promise((r) => setTimeout(r, 1100));

    await waitFor(() => {
      expect(screen.getByTestId("success")).toBeVisible();
    });
  });

  it("bruker organisasjonsnummeret fra forespørselen i arbeidsgiversvaret", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    const forespurtOrgnummer = "123456789";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);
    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );

    mockFetchResponse({ status: 200, text: () => "" });
    await userEvent.click(screen.getByText("Opprett dokumenter"));
    await waitFor(() => expect(subscribeMock).toHaveBeenCalled());

    const callback = subscribeMock.mock.calls.at(-1)?.[1];
    expect(callback).toBeDefined();
    await act(async () => callback?.("vedtaksperiode-id", forespurtOrgnummer));

    await waitFor(() => {
      const innsendtKall = (fetch as Mock).mock.calls
        .filter(([url]) => url === "http://0.0.0.0:8080/vedtaksperiode")
        .at(-1);
      const payload = JSON.parse(innsendtKall?.[1]?.body ?? "{}");
      expect(payload.orgnummer).toBe(forespurtOrgnummer);
    });
  });

  it("sender ikke arbeidsgiversvar dersom forespørselen mangler organisasjonsnummer", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);
    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );

    mockFetchResponse({ status: 200, text: () => "" });
    await userEvent.click(screen.getByText("Opprett dokumenter"));
    await waitFor(() => expect(subscribeMock).toHaveBeenCalled());

    const antallKallFørCallback = (fetch as Mock).mock.calls.length;
    const callback = subscribeMock.mock.calls.at(-1)?.[1];
    expect(callback).toBeDefined();
    await act(async () => callback?.("vedtaksperiode-id", ""));

    expect((fetch as Mock).mock.calls.length).toBe(antallKallFørCallback);
  });

  it("viser lenke til loggene for tracen til dokumentene og arbeidsgiversvaret", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);
    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );

    mockFetchResponse({
      status: 200,
      text: () => JSON.stringify({ traceUrl: "https://trace/dokumenter" }),
    });
    await userEvent.click(screen.getByText("Opprett dokumenter"));
    await waitFor(() =>
      expect(screen.getByRole("link", { name: /Dokumenter/ })).toHaveAttribute(
        "href",
        "https://trace/dokumenter",
      ),
    );

    mockFetchResponse({
      status: 200,
      text: () => JSON.stringify({ traceUrl: "https://trace/arbeidsgiver" }),
    });
    const callback = subscribeMock.mock.calls.at(-1)?.[1];
    await act(async () => callback?.("vedtaksperiode-id", orgnr));
    await waitFor(() =>
      expect(
        screen.getByRole("link", { name: /Arbeidsgiveropplysninger/ }),
      ).toHaveAttribute("href", "https://trace/arbeidsgiver"),
    );
    expect(screen.getByRole("link", { name: /Dokumenter/ })).toBeVisible();
  });

  it("viser ikke vedtaksperiode-ID når søknad sendes", async () => {
    render(<OpprettDokumenter />, { wrapper });

    expect(screen.queryByTestId("vedtaksperiodeId")).toBeNull();
    await userEvent.click(screen.getByLabelText("Send sykmelding"));
    expect(screen.queryByTestId("vedtaksperiodeId")).toBeNull();
  });

  it("sender bare arbeidsgiveropplysninger med oppgitt vedtaksperiode-ID", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    const vedtaksperiodeId = "f75c33dd-3c89-4305-8de8-472a7e598f82";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);
    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );
    await userEvent.click(screen.getByLabelText("Send sykmelding"));
    await userEvent.click(screen.getByLabelText("Send søknad"));

    await userEvent.click(screen.getByText("Opprett dokumenter"));
    await waitFor(() =>
      expect(screen.getByText("Vedtaksperiode-ID må fylles ut")).toBeVisible(),
    );
    await userEvent.click(
      screen.getByRole("button", { name: "Hvor finner jeg vedtaksperiode-ID?" }),
    );
    expect(screen.getByText(/Alt/)).toBeVisible();

    await userEvent.type(
      screen.getByTestId("vedtaksperiodeId"),
      vedtaksperiodeId,
    );
    mockFetchResponse({ status: 200, text: () => "" });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    let innsendteKall: unknown[][] = [];
    await waitFor(() => {
      innsendteKall = (fetch as Mock).mock.calls.filter(
        ([url]) => url === "http://0.0.0.0:8080/vedtaksperiode",
      );
      expect(innsendteKall).toHaveLength(1);
    });
    const payload = JSON.parse(
      (innsendteKall[0][1] as { body: string }).body,
    );
    expect(payload.orgnummer).toBe(orgnr);
    expect(payload.sykmelding).toBeUndefined();
    expect(payload.søknad).toBeUndefined();
    expect(payload.medlemskapVerdi).toBeUndefined();
    expect(payload.arbeidsgiveropplysninger).toMatchObject({
      inntekt: "54321",
      vedtaksperiodeId,
      forespurt: true,
      arsakTilInnsending: "Ny",
    });
    expect(subscribeMock).not.toHaveBeenCalled();
  });

  const fyllUtOgSendInn = async (velgMedlemskap?: () => Promise<void>) => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);

    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);
    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );
    await velgMedlemskap?.();

    mockFetchResponse({ status: 200, text: () => "" });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    let payload: Record<string, any> = {};
    await waitFor(() => {
      const innsendtKall = (fetch as Mock).mock.calls
        .filter(([url]) => url === "http://0.0.0.0:8080/vedtaksperiode")
        .at(-1);
      expect(innsendtKall).toBeDefined();
      payload = JSON.parse(innsendtKall?.[1]?.body ?? "{}");
    });
    return payload;
  };

  it("sender medlemskap JA uten årsaker som standard", async () => {
    const payload = await fyllUtOgSendInn();

    expect(screen.queryByTestId("leggTilMedlemskapAarsak")).toBeNull();
    expect(payload.medlemskapVerdi).toBe("JA");
    expect(payload.medlemskapAarsaker).toEqual([]);
  });

  it("sender valgt medlemskapsvurdering med årsaker", async () => {
    const payload = await fyllUtOgSendInn(async () => {
      await userEvent.selectOptions(
        screen.getByTestId("medlemskapVerdi"),
        "UAVKLART_MED_BRUKERSPORSMAAL",
      );
      await userEvent.click(screen.getByTestId("leggTilMedlemskapAarsak"));
      await userEvent.type(
        screen.getByTestId("medlemskapAarsakRegelId0"),
        "REGEL_3",
      );
      await userEvent.type(
        screen.getByTestId("medlemskapAarsakForklaring0"),
        "Ikke registrert arbeidsforhold",
      );
    });

    expect(payload.medlemskapVerdi).toBe("UAVKLART_MED_BRUKERSPORSMAAL");
    expect(payload.medlemskapAarsaker).toEqual([
      {
        regelId: "REGEL_3",
        forklaringPaaUavklart: "Ikke registrert arbeidsforhold",
      },
    ]);
  });

  it("krever fødselsnummer, organisasjonsnummer og inntekt", async () => {
    render(<OpprettDokumenter />, { wrapper });

    await userEvent.click(screen.getByText("Opprett dokumenter"));

    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
      expect(
        screen.getByText("Organisasjonsnummer må fylles ut"),
      ).toBeVisible();
      expect(screen.getByText("Inntekt må angis")).toBeVisible();
    });
  });

  it("mapper skjemaverdier til payload", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);
    await userEvent.type(screen.getByTestId("fnr"), "01234567890");

    await waitFor(() => {
      expect(fetch as Mock).toHaveBeenCalledWith(
        "http://0.0.0.0:8080/person/inntekt",
        {
          headers: { Accept: "application/json", ident: "01234567890" },
          method: "get",
        },
      );
    });

    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    await userEvent.type(screen.getByTestId("faktiskgrad"), "80");
    skrivDato("sykdomFom", "2021-07-01");
    skrivDato("sykdomTom", "2021-07-31");

    await userEvent.clear(screen.getByTestId("refusjonsbeløp"));
    await userEvent.type(screen.getByTestId("refusjonsbeløp"), "20000");
    skrivDato("opphørRefusjon", "2021-08-01");

    await userEvent.click(screen.getByTestId("arbeidsgiverperioderButton"));
    skrivDato("arbeidsgiverFom0", "2021-07-01");
    skrivDato("arbeidsgiverTom0", "2021-07-16");

    await userEvent.click(screen.getByTestId("ferieButton"));
    skrivDato("ferieFom0", "2021-07-02");
    skrivDato("ferieTom0", "2021-07-04");

    await userEvent.click(screen.getByTestId("endringButton"));
    skrivDato("endringsdato0", "2021-07-17");
    await userEvent.type(screen.getByTestId("endringsbeløp0"), "19000");
    mockFetchResponse({ status: 200, text: () => vi.fn() });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    await new Promise((r) => setTimeout(r, 1100));

    await waitFor(() => {
      expect(fetch as Mock).toHaveBeenLastCalledWith(
        "http://0.0.0.0:8080/vedtaksperiode",
        {
          body: `${JSON.stringify({
            fnr: "01234567890",
            orgnummer: "987654321",
            sykdomFom: "2021-07-01",
            sykdomTom: "2021-07-31",
            arbeidssituasjon: "ARBEIDSTAKER",
            sykmelding: { sykmeldingsgrad: "100" },
            søknad: {
              sykmeldingsgrad: "100",
              harAndreInntektskilder: false,
              ferieperioder: [{ fom: "2021-07-02", tom: "2021-07-04" }],
              faktiskgrad: "80",
              sendtNav: "2021-08-01",
              tidligereArbeidsgiverOrgnummer: null,
              inntektFraSigrun: null,
              fraværFørSykmeldingen: null,
              harBrukerOppgittForsikring: null,
              meldingTilNavDagerFraSykmelding: null,
            },
            medlemskapVerdi: "JA",
            medlemskapAarsaker: [],
          })}`,
          headers: { "Content-Type": "application/json" },
          method: "post",
        },
      );
    });
  });

  it("sletter person", async () => {
    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);
    mockFetchResponse({ status: 204 });

    render(<OpprettDokumenter />, { wrapper });

    const fnr = "12345678900";
    await userEvent.type(screen.getByTestId("fnr"), fnr);

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        `http://0.0.0.0:8080/organisasjon/${orgnr}`,
        { headers: { Accept: "application/json" }, method: "get" },
      );
    });

    await userEvent.click(screen.getByText("Slett"));

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith("http://0.0.0.0:8080/person", {
        headers: { ident: fnr },
        method: "delete",
      });
    });
  });

  it("viser feilmelding om sletting feiler", async () => {
    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);
    mockFetchResponse({ status: 404, text: () => vi.fn() });

    render(<OpprettDokumenter />, { wrapper });

    await userEvent.type(screen.getByTestId("fnr"), "12345678900");
    await waitFor(() => {
      expect(fetch).toHaveBeenCalledWith(
        `http://0.0.0.0:8080/organisasjon/${orgnr}`,
        { headers: { Accept: "application/json" }, method: "get" },
      );
    });
    await userEvent.click(screen.getByText("Slett"));
3
    await waitFor(() => {
      expect(screen.getByText("Sletting av person feilet mot backend")).toBeVisible();
    });
  });

  it("endring av sykdomTom endrer automatisk søknadSendt til sykdomTom + 1", async () => {
    render(<OpprettDokumenter />, { wrapper });

    skrivDato("sykdomTom", "2021-08-31");

    await waitFor(() => {
      expect(screen.getByTestId("sendtNav")).toHaveValue(visDato("2021-09-01"));
    });
  });

  it("endring av sykdomFom endrer automatisk førsteFraværsdag til sykdomFom", async () => {
    render(<OpprettDokumenter />, { wrapper });

    skrivDato("sykdomFom", "2021-07-31");

    await waitFor(() => {
      expect(screen.getByTestId("førsteFraværsdag")).toHaveValue(
        visDato("2021-07-31"),
      );
    });
  });

  it("viser meldingTilNavDagerFraSykmelding-felter kun for SELVSTENDIG_NARINGSDRIVENDE", async () => {
    render(<OpprettDokumenter />, { wrapper });

    // Skal ikke vises for ARBEIDSTAKER (default)
    expect(
      screen.queryByTestId("meldingTilNavDagerFraSykmeldingFom"),
    ).not.toBeInTheDocument();
    expect(
      screen.queryByTestId("meldingTilNavDagerFraSykmeldingTom"),
    ).not.toBeInTheDocument();

    // Bytt til SELVSTENDIG_NARINGSDRIVENDE
    fireEvent.change(screen.getAllByRole("combobox")[0], {
      target: { value: "SELVSTENDIG_NARINGSDRIVENDE" },
    });

    await waitFor(() => {
      expect(
        screen.getByTestId("meldingTilNavDagerFraSykmeldingFom"),
      ).toBeInTheDocument();
      expect(
        screen.getByTestId("meldingTilNavDagerFraSykmeldingTom"),
      ).toBeInTheDocument();
    });
  });

  it("sender meldingTilNavDagerFraSykmelding som periode når feltene er fylt ut for SELVSTENDIG", async () => {
    render(<OpprettDokumenter />, { wrapper });

    // Bytt til SELVSTENDIG_NARINGSDRIVENDE FØR fnr skrives inn, slik at
    // InntektsmeldingCard forsvinner før fetch-kallene trigges
    fireEvent.change(screen.getAllByRole("combobox")[0], {
      target: { value: "SELVSTENDIG_NARINGSDRIVENDE" },
    });

    // Nå er InntektsmeldingCard borte; fnr-typing trigger bare 2 fetches:
    // 1) person-navn, 2) arbeidsforhold
    mockPersonNavn();
    mockFetchResponse({ json: () => ({ arbeidsforhold: [] }) });

    await userEvent.type(screen.getByTestId("fnr"), "11111111111");

    // Fyll inn inntektFraSigrun (påkrevd for SELVSTENDIG)
    await waitFor(() =>
      expect(
        screen.getByLabelText(/Årsinntekt fra Sigrun/),
      ).toBeInTheDocument(),
    );
    await userEvent.type(
      screen.getByLabelText(/Årsinntekt fra Sigrun/),
      "600000",
    );

    // Fyll inn periodefelter
    skrivDato("meldingTilNavDagerFraSykmeldingFom", "2021-07-01");
    skrivDato("meldingTilNavDagerFraSykmeldingTom", "2021-07-31");

    mockFetchResponse({ status: 200, text: () => vi.fn() });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    await waitFor(() => {
      const [, lastCall] = (fetch as Mock).mock.calls.slice(-1);
      expect(lastCall).toBeUndefined(); // fetch was called
      const body = JSON.parse(
        ((fetch as Mock).mock.calls.find(
          ([url]) => url === "http://0.0.0.0:8080/vedtaksperiode",
        ) ?? [])[1]?.body ?? "{}",
      );
      expect(body.søknad?.meldingTilNavDagerFraSykmelding).toEqual({
        fom: "2021-07-01",
        tom: "2021-07-31",
      });
    });
  });

  it("sender meldingTilNavDagerFraSykmelding som null når feltene ikke er fylt ut", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockPersonNavn();
    mockArbeidsforhold(orgnr);
    mockStandardInntekt(orgnr, "54321");
    mockOrganisasjonnavn(orgnr);
    await userEvent.type(screen.getByTestId("fnr"), "01234567890");
    await userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        "54321",
      ),
    );

    mockFetchResponse({ status: 200, text: () => vi.fn() });
    await userEvent.click(screen.getByText("Opprett dokumenter"));

    await new Promise((r) => setTimeout(r, 1100));

    await waitFor(() => {
      const vedtaksperiodeCall = (fetch as Mock).mock.calls.find(
        ([url]) => url === "http://0.0.0.0:8080/vedtaksperiode",
      );
      const body = JSON.parse(vedtaksperiodeCall?.[1]?.body ?? "{}");
      expect(body.søknad?.meldingTilNavDagerFraSykmelding).toBeNull();
    });
  });

  const mockStandardInntekt = (orgnr: string, månedsinntekt: string) =>
    mockFetchResponse({
      json: () => ({
        beregnetMånedsinntekt: månedsinntekt,
        arbeidsgivere: [
          { organisasjonsnummer: orgnr, beregnetMånedsinntekt: månedsinntekt },
        ],
      }),
    });

  const mockArbeidsforhold = (orgnr: string) =>
    mockFetchResponse({
      json: () => ({
        arbeidsforhold: [
          {
            type: "ORDINÆRT",
            arbeidsgiver: {
              type: "Organisasjon",
              identifikator: orgnr,
            },
            detaljer: [
              {
                yrke: "UTVIKLER",
              },
            ],
          },
        ],
      }),
    });

  const mockPersonNavn = () =>
    mockFetchResponse({
      json: () => ({
        fornavn: "NORMAL",
        mellomnavn: null,
        etternavn: "MUFFINS",
      }),
    });

  const mockOrganisasjonnavn = (orgnr: string) =>
    mockFetchResponse({
      json: () => ({
        navn: `Testnavn for ${orgnr}`,
      }),
    });
});
