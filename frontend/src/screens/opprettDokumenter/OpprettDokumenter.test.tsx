import React, { ReactNode } from "react";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { OpprettDokumenter } from "./OpprettDokumenter";
import { AppProvider } from "../../state/AppContext";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, Mock, vi } from "vitest";

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
    fireEvent.change(screen.getByTestId("sykdomFom"), {
      target: { value: "2021-07-01" },
    });
    fireEvent.change(screen.getByTestId("sykdomTom"), {
      target: { value: "2021-07-31" },
    });

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
              ventetidFom: null,
              ventetidTom: null,
              fraværFørSykmeldingen: null,
              harBrukerOppgittForsikring: null,
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

  it("endring av sykdomTom endrer automatisk søknadSendt til sykdomTom + 1", async () => {
    render(<OpprettDokumenter />, { wrapper });

    fireEvent.change(screen.getByTestId("sykdomTom"), {
      target: { value: "2021-08-31" },
    });

    await waitFor(() => {
      expect(screen.getByTestId("sendtNav")).toHaveValue("2021-09-01");
    });
  });

  it("endring av sykdomFom endrer automatisk førsteFraværsdag til sykdomFom", async () => {
    render(<OpprettDokumenter />, { wrapper });

    fireEvent.change(screen.getByTestId("sykdomFom"), {
      target: { value: "2021-07-31" },
    });

    await waitFor(() => {
      expect(screen.getByTestId("førsteFraværsdag")).toHaveValue("2021-07-31");
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
