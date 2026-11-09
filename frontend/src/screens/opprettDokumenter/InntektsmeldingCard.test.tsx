import React from "react";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { FormProvider, useForm } from "react-hook-form";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { InntektsmeldingCard } from "./InntektsmeldingCard";

vi.mock("../../io/environment", () => ({
  Environment: { Mode: "development" },
}));

const fetchMock = vi.fn<typeof fetch>();
global.fetch = fetchMock;

const inntektsrespons = (inntekt: number): Response =>
  new Response(
    JSON.stringify({
      arbeidsgivere: [
        {
          organisasjonsnummer: "987654321",
          beregnetMånedsinntekt: inntekt,
        },
        {
          organisasjonsnummer: "123456789",
          beregnetMånedsinntekt: 20000,
        },
      ],
    }),
    { status: 200 },
  );

const Skjema = () => {
  const form = useForm({
    defaultValues: {
      fnr: "01234567890",
      orgnummer: "987654321",
      skalSendeSykmelding: true,
      inntektsmelding: { førsteFraværsdag: "2025-01-15" },
    },
  });

  return (
    <FormProvider {...form}>
      <input aria-label="Fødselsnummer" {...form.register("fnr")} />
      <input aria-label="Organisasjonsnummer" {...form.register("orgnummer")} />
      <InntektsmeldingCard />
    </FormProvider>
  );
};

const endreFraværsdag = (dato: string) =>
  fireEvent.change(screen.getByTestId("førsteFraværsdag"), {
    target: { value: dato },
  });

describe("InntektsmeldingCard", () => {
  beforeEach(() => {
    fetchMock.mockReset();
    fetchMock.mockImplementation(async () => inntektsrespons(30000));
  });

  it("bruker første fraværsdag i inntektsoppslaget og fyller inn inntekt og refusjon", async () => {
    render(<Skjema />);

    await waitFor(() => {
      expect(screen.getByTestId("inntekt")).toHaveValue("30000");
      expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("30000");
    });
    expect(fetchMock).toHaveBeenCalledExactlyOnceWith(
      "http://0.0.0.0:8080/person/inntekt?foersteFravaersdag=2025-01-15",
      expect.objectContaining({
        headers: expect.objectContaining({ ident: "01234567890" }),
      }),
    );
  });

  it("henter og fyller inn ny inntekt når første fraværsdag endres", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );
    fetchMock.mockImplementation(async () => inntektsrespons(45000));

    endreFraværsdag("15.02.2025");

    expect(screen.getByTestId("inntekt")).toHaveValue("");
    expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("");
    await waitFor(() => {
      expect(screen.getByTestId("inntekt")).toHaveValue("45000");
      expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("45000");
    });
    expect(fetchMock).toHaveBeenLastCalledWith(
      "http://0.0.0.0:8080/person/inntekt?foersteFravaersdag=2025-02-15",
      expect.anything(),
    );
  });

  it("bruker inntekten for riktig dato når en tidligere valgt dato velges igjen", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );
    fetchMock.mockImplementation(async () => inntektsrespons(45000));
    endreFraværsdag("15.02.2025");
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("45000"),
    );

    endreFraværsdag("15.01.2025");

    await waitFor(() => {
      expect(screen.getByTestId("inntekt")).toHaveValue("30000");
      expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("30000");
    });
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("bruker inntekten for valgt arbeidsgiver uten et nytt oppslag", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );

    fireEvent.change(screen.getByLabelText("Organisasjonsnummer"), {
      target: { value: "123456789" },
    });

    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("20000"),
    );
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("henter inntekt på nytt når fødselsnummeret endres", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );
    fetchMock.mockImplementation(async () => inntektsrespons(45000));

    fireEvent.change(screen.getByLabelText("Fødselsnummer"), {
      target: { value: "12345678901" },
    });

    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("45000"),
    );
    expect(fetchMock).toHaveBeenLastCalledWith(
      expect.stringContaining("foersteFravaersdag=2025-01-15"),
      expect.objectContaining({
        headers: expect.objectContaining({ ident: "12345678901" }),
      }),
    );
  });

  it("henter ikke inntekt når første fraværsdag er ugyldig", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );

    endreFraværsdag("ugyldig");

    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("tømmer tidligere inntekt når den nye perioden ikke har inntekter", async () => {
    render(<Skjema />);
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );
    fetchMock.mockImplementation(
      async () =>
        new Response(JSON.stringify({ arbeidsgivere: [] }), { status: 200 }),
    );

    endreFraværsdag("15.02.2025");

    await waitFor(() => {
      expect(screen.getByTestId("inntekt")).toHaveValue("");
      expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("");
    });
  });

  it("viser feil når inntektsoppslaget feiler og fjerner feilen ved neste vellykkede oppslag", async () => {
    fetchMock.mockResolvedValueOnce(new Response("", { status: 500 }));
    render(<Skjema />);

    expect(
      await screen.findByText(
        "Kunne ikke hente inntekt for valgt dato. Fyll inn inntekt og refusjonsbeløp manuelt.",
      ),
    ).toBeVisible();
    endreFraværsdag("15.02.2025");

    await waitFor(() => {
      expect(screen.getByTestId("inntekt")).toHaveValue("30000");
      expect(screen.queryByText(/Kunne ikke hente inntekt/)).toBeNull();
    });
  });

  it("lar ikke et forsinket svar overskrive inntekten for den nye datoen", async () => {
    let svarPåFørsteOppslag: (response: Response) => void = () => {
      throw new Error("Det første oppslaget er ikke startet");
    };
    fetchMock.mockImplementationOnce(
      () =>
        new Promise<Response>((resolve) => {
          svarPåFørsteOppslag = resolve;
        }),
    );
    render(<Skjema />);
    await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(1));

    endreFraværsdag("15.02.2025");
    await waitFor(() =>
      expect(screen.getByTestId("inntekt")).toHaveValue("30000"),
    );

    await act(async () => svarPåFørsteOppslag(inntektsrespons(10000)));

    expect(screen.getByTestId("inntekt")).toHaveValue("30000");
    expect(screen.getByTestId("refusjonsbeløp")).toHaveValue("30000");
  });
});
