import React from "react";
import { render, screen, waitFor } from "@testing-library/react";
import "@testing-library/jest-dom";
import { OpprettDokumenter } from "./OpprettDokumenter";
import { RecoilRoot } from "recoil";
import userEvent from "@testing-library/user-event";

jest.mock("../../io/subscription", () => ({
  useSubscribe: () => [() => {}],
}));

jest.mock("../../io/environment", () => ({
  Environment: {
    Mode: "development",
  },
}));

global.fetch = jest.fn();

const mockFetchResponse = (body: object) =>
  (fetch as jest.Mock).mockImplementationOnce(() => Promise.resolve(body));

const wrapper = ({ children }) => <RecoilRoot>{children}</RecoilRoot>;

describe("OpprettDokumenter", () => {
  it("oppretter dokumenter", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    const inntekt = "54321";
    mockStandardInntekt(orgnr, inntekt)

    userEvent.type(screen.getByTestId("fnr"), "01234567890");
    userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(inntekt)
    );
    mockFetchResponse({ status: 200, text: () => jest.fn() });
    userEvent.click(screen.getByText("Opprett dokumenter"));

    await new Promise((r) => setTimeout(r, 1100));

    await waitFor(() => {
      expect(screen.getByTestId("success")).toBeVisible();
    });
  });

  it("krever fødselsnummer, organisasjonsnummer og inntekt", async () => {
    render(<OpprettDokumenter />, { wrapper });

    userEvent.click(screen.getByText("Opprett dokumenter"));

    await waitFor(() => {
      expect(screen.getByText("Fødselsnummer må fylles ut")).toBeVisible();
      expect(
        screen.getByText("Organisasjonsnummer må fylles ut")
      ).toBeVisible();
      expect(screen.getByText("Inntekt må angis")).toBeVisible();
    });
  });

  it("mapper skjemaverdier til payload", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    mockStandardInntekt(orgnr, "54321");

    userEvent.type(screen.getByTestId("fnr"), "01234567890");

    await waitFor(() => {
      expect(fetch as jest.Mock).toHaveBeenCalledWith(
        "http://0.0.0.0:8080/person/inntekt",
        {
          headers: { Accept: "application/json", ident: "01234567890" },
          method: "get",
        }
      );
    });

    userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    userEvent.type(screen.getByTestId("faktiskgrad"), "80");
    userEvent.type(screen.getByTestId("sykdomFom"), "2021-07-01");
    userEvent.type(screen.getByTestId("sykdomTom"), "2021-07-31");

    userEvent.clear(screen.getByTestId("refusjonsbeløp"))
    userEvent.type(screen.getByTestId("refusjonsbeløp"), "20000")
    userEvent.type(screen.getByTestId("opphørRefusjon"), "2021-08-01")

    userEvent.click(screen.getByTestId("arbeidsgiverperioderButton"));
    userEvent.type(screen.getByTestId("arbeidsgiverFom0"), "2021-07-01");
    userEvent.type(screen.getByTestId("arbeidsgiverTom0"), "2021-07-16");

    userEvent.click(screen.getByTestId("ferieButton"));
    userEvent.type(screen.getByTestId("ferieFom0"), "2021-07-02");
    userEvent.type(screen.getByTestId("ferieTom0"), "2021-07-04");

    userEvent.click(screen.getByTestId("endringButton"));
    userEvent.type(screen.getByTestId("endringsdato0"), "2021-07-17");
    userEvent.type(screen.getByTestId("endringsbeløp0"), "19000");
    mockFetchResponse({ status: 200,text: () => jest.fn() });
    userEvent.click(screen.getByText("Opprett dokumenter"));

    await new Promise((r) => setTimeout(r, 1100));

    await waitFor(() => {
      expect(fetch as jest.Mock).toHaveBeenLastCalledWith(
        "http://0.0.0.0:8080/vedtaksperiode",
        {
          body: `${JSON.stringify({
            fnr: "01234567890",
            orgnummer: "987654321",
            sykdomFom: "2021-07-01",
            sykdomTom: "2021-07-31",
            sykmelding: { sykmeldingsgrad: "100" },
            søknad: {
              sykmeldingsgrad: "100",
              harAndreInntektskilder: false,
              ferieperioder: [{ fom: "2021-07-02", tom: "2021-07-04" }],
              faktiskgrad: "80",
              sendtNav: "2021-08-01",
            },
            medlemskapAvklart: true,
            inntektsmelding: {
              inntekt: "54321",
              refusjon: {
                opphørRefusjon: "2021-08-01",
                refusjonsbeløp: "20000"
              },
              arbeidsgiverperiode: [{ fom: "2021-07-01", tom: "2021-07-16" }],
              endringRefusjon: [{ endringsdato: "2021-07-17", beløp: "19000"}],
              førsteFraværsdag: "2021-07-01",
              begrunnelseForReduksjonEllerIkkeUtbetalt: "",
              harOpphørAvNaturalytelser: false
            },
          })}`,
          headers: { "Content-Type": "application/json" },
          method: "post",
        }
      );
    });
  });

  it("sletter person", async () => {
    render(<OpprettDokumenter />, { wrapper });

    const orgnr = "987654321";
    const inntekt = "54321"
    mockStandardInntekt(orgnr, inntekt);
    userEvent.type(screen.getByTestId("fnr"), "01234567890");
    userEvent.click(screen.getByTestId("slettPerson"));
    userEvent.type(screen.getByTestId("orgnummer"), orgnr);

    await waitFor(() =>
      expect(screen.getByRole("textbox", { name: /Inntekt/ })).toHaveValue(
        inntekt
      )
    );

    mockFetchResponse({ status: 200, text: () => jest.fn() });
    userEvent.click(screen.getByText("Opprett dokumenter"));

    await waitFor(() => {
      expect(fetch as jest.Mock).toHaveBeenLastCalledWith(
        "http://0.0.0.0:8080/person",
        { headers: { ident: "01234567890" }, method: "delete" }
      );
    });
  });

  it("endring av sykdomTom endrer automatisk søknadSendt til sykdomTom + 1", async () => {
    render(<OpprettDokumenter />, { wrapper });

    userEvent.type(screen.getByTestId("sykdomTom"), "2021-08-31");

    await waitFor(() => {
      expect(screen.getByTestId("sendtNav")).toHaveValue("2021-09-01");
    });
  });

  it("endring av sykdomFom endrer automatisk førsteFraværsdag til sykdomFom", async () => {
    render(<OpprettDokumenter />, { wrapper });

    userEvent.type(screen.getByTestId("sykdomFom"), "2021-07-31");

    await waitFor(() => {
      expect(screen.getByTestId("førsteFraværsdag")).toHaveValue("2021-07-31");
    });
  });

  const mockStandardInntekt = (orgnr: string, månedsinntekt: string) => {
    mockFetchResponse({
      json: () => ({
        beregnetMånedsinntekt: månedsinntekt,
        arbeidsgivere: [
          { organisasjonsnummer: orgnr, beregnetMånedsinntekt: månedsinntekt },
        ],
      }),
    });
  };
});
