import React, { ReactNode } from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { OpprettDokumenter } from "./OpprettDokumenter";
import { AppProvider } from "../../state/AppContext";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";
import { format, startOfMonth, subMonths} from "date-fns";

vi.mock("../../io/subscription", () => ({
  useSubscribe: () => [() => {}],
}));

vi.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = vi.fn();

const mockFetchResponse = (body: object) =>
  (fetch as Mock).mockImplementationOnce(() => Promise.resolve(body));

const wrapper = ({ children }: { children: ReactNode }) => (
  <AppProvider>{children}</AppProvider>
);
describe("OpprettDokumenter", () => {
  beforeEach(() => {
    vi.clearAllMocks();
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
    const sykdomFraInput = screen.getByLabelText("Sykdom fra");
    await userEvent.clear(sykdomFraInput);
    await userEvent.type(sykdomFraInput, "01.07.2021");

    const sykdomTilInput = screen.getByLabelText("Sykdom til");
    await userEvent.clear(sykdomTilInput);
    await userEvent.type(sykdomTilInput, "31.07.2021");

    await userEvent.clear(screen.getByTestId("refusjonsbeløp"));
    await userEvent.type(screen.getByTestId("refusjonsbeløp"), "20000");
    fireEvent.change(screen.getByTestId("opphørRefusjon"), {
      target: { value: "2021-08-01" },
    });

    await userEvent.click(screen.getByTestId("arbeidsgiverperioderButton"));
    fireEvent.change(screen.getByTestId("arbeidsgiverFom0"), {
      target: { value: "2021-07-01" },
    });
    fireEvent.change(screen.getByTestId("arbeidsgiverTom0"), {
      target: { value: "2021-07-16" },
    });

    await userEvent.click(screen.getByTestId("ferieButton"));
    fireEvent.change(screen.getByTestId("ferieFom0"), {
      target: { value: "2021-07-02" },
    });
    fireEvent.change(screen.getByTestId("ferieTom0"), {
      target: { value: "2021-07-04" },
    });

    await userEvent.click(screen.getByTestId("endringButton"));
    fireEvent.change(screen.getByTestId("endringsdato0"), {
      target: { value: "2021-07-17" },
    });
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
            inntektsmelding: {
              inntekt: "54321",
              refusjon: {
                opphørRefusjon: "2021-08-01",
                refusjonsbeløp: "20000",
              },
              arbeidsgiverperiode: [{ fom: "2021-07-01", tom: "2021-07-16" }],
              endringRefusjon: [{ endringsdato: "2021-07-17", beløp: "19000" }],
              førsteFraværsdag: "2021-07-01",
              begrunnelseForReduksjonEllerIkkeUtbetalt: "",
              harOpphørAvNaturalytelser: false,
            },
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
      expect(fetch).toHaveBeenNthCalledWith(
        4,
        `http://0.0.0.0:8080/organisasjon/${orgnr}`,
        { headers: { Accept: "application/json" }, method: "get" },
      );
    });

    await userEvent.click(screen.getByText("❌"));

    await waitFor(() => {
      expect(fetch).toHaveBeenNthCalledWith(5, "http://0.0.0.0:8080/person", {
        headers: { ident: fnr },
        method: "delete",
      });
      expect(screen.getByText("✔️️")).toBeVisible();
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
      expect(fetch).toHaveBeenNthCalledWith(
        4,
        `http://0.0.0.0:8080/organisasjon/${orgnr}`,
        { headers: { Accept: "application/json" }, method: "get" },
      );
    });
    await userEvent.click(screen.getByText("❌"));

    await waitFor(() => {
      expect(screen.getByText("☠️")).toBeVisible();
      expect(screen.getByText("Sletting av person feilet")).toBeVisible();
    });
  });

  it("endring av Sykdom til endrer sendtNav til dagen etter", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const target = screen.getByLabelText("Søknad sendt Nav");
    const nå = new Date();

    // Samme logikk som i produksjonskoden. Kan også bare sjekke at verdien er ulik det vi expecter nederst i testen
    const dagenEtterForTreMånederSiden = format(startOfMonth(subMonths(nå, 2)), "yyyy-MM-dd");
    expect(target).toHaveValue(dagenEtterForTreMånederSiden);

    // Sykdom fra må være satt for at til-feltet skal validere ok
    const sykdomFraInput = screen.getByLabelText("Sykdom fra");
    await userEvent.clear(sykdomFraInput);
    await userEvent.type(sykdomFraInput, "01.08.2021");

    const sykdomTilInput = screen.getByLabelText("Sykdom til");
    await userEvent.clear(sykdomTilInput);
    await userEvent.type(sykdomTilInput, "31.08.2021");

    await waitFor(() => {
      expect(target).toHaveValue("2021-09-01");
    });
  });

  it("endring av sykdom fra endrer førsteFraværsdag til samme dato", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const target = screen.getByLabelText("Første fraværsdag");
    expect(target).not.toHaveValue("2021-07-31");

    const sykdomFraInput = screen.getByLabelText("Sykdom fra");
    await userEvent.clear(sykdomFraInput);
    await userEvent.type(sykdomFraInput, "31.07.2021");

    await waitFor(() => {
      expect(target).toHaveValue("2021-07-31");
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
    fireEvent.change(
      screen.getAllByRole("combobox")[0],
      { target: { value: "SELVSTENDIG_NARINGSDRIVENDE" } },
    );

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
      expect(screen.getByLabelText(/Årsinntekt fra Sigrun/)).toBeInTheDocument(),
    );
    await userEvent.type(screen.getByLabelText(/Årsinntekt fra Sigrun/), "600000");

    // Fyll inn periodefelter
    fireEvent.change(screen.getByTestId("meldingTilNavDagerFraSykmeldingFom"), {
      target: { value: "2021-07-01" },
    });
    fireEvent.change(screen.getByTestId("meldingTilNavDagerFraSykmeldingTom"), {
      target: { value: "2021-07-31" },
    });

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
